package com.longquan.common.sync;

import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

/**
 * author : charile yuan
 * date   : 21-3-8
 * desc   :
 */
public class CommandCenter {
    private static final String TAG = "CommandCenter";
    private static final int COMMAND_TIMEOUT = 5000;
    private CommandCenter.CenterState mCenterStat;
    private Queue mCommandQueue;
    private Thread mCommandExec;
    private int mTimeout;


    public CommandCenter() {
        this.mCenterStat = CommandCenter.CenterState.FREE;
        this.mCommandQueue = new LinkedList();
        this.mTimeout = 5000;
        this.mTimeout = 5000;
    }

    public CommandCenter(int timeout) {
        this.mCenterStat = CommandCenter.CenterState.FREE;
        this.mCommandQueue = new LinkedList();
        this.mTimeout = 5000;
        this.mTimeout = timeout;
    }

    public void execInQueue(ICommand command) {
        this.print("execInQueue[" + command.getName() + "]");
        this.listAddCommand(command);
        if (this.mCommandExec != null && this.mCenterStat == CommandCenter.CenterState.BUSY) {
            this.print("execInQueue wait");
        } else {
            this.exec();
        }
    }

    public void execInForce(ICommand command) {
        this.print("execInForce[" + command.getName() + "]");
        this.listAddHeadCommand(command);
        if (this.mCommandExec != null && this.mCenterStat == CommandCenter.CenterState.BUSY) {
            this.print("execInForce force");
            this.force();
        }

        this.exec();
    }

    public void execInOnly(ICommand command) {
        this.print("execInForce[" + command.getName() + "]");
        this.listAddClearCommand(command);
        if (this.mCommandExec != null && this.mCenterStat == CommandCenter.CenterState.BUSY) {
            this.print("execInForce force");
            this.force();
        }

        this.exec();
    }

    public void execInFirst(ICommand command) {
        this.print("execQuick[" + command.getName() + "]");
        this.listAddHeadCommand(command);
        this.exec();
    }

    public void kill() {
        this.print("kill");
        this.mCommandQueue.clear();
        if (this.mCommandExec != null && this.mCenterStat == CommandCenter.CenterState.BUSY) {
            this.print("execInForce force");
            this.force();
        }

    }

    private void exec() {
        this.mCommandExec = new Thread("CommandCenter") {

            private ICommand iCommand;

            @Override
            public void run() {
                while (CommandCenter.this.listHasCommand()) {
                    this.iCommand = CommandCenter.this.listNextCommandNext();
                    if (this.iCommand != null) {
                        CommandCenter.this.mCenterStat = CommandCenter.CenterState.BUSY;
                        CommandCenter.this.print("exec start[" + this.iCommand.getName() + "]");
                        this.iCommand.setDaemon(true);
                        this.iCommand.start();

                        try {
                            CommandCenter.this.print("exec join start［"
                                    + this.iCommand.getName() + "][" + CommandCenter.this.mTimeout + "]");
                            this.iCommand.join((long) CommandCenter.this.mTimeout);
                            CommandCenter.this.print("exec join finish［"
                                    + this.iCommand.getName() + "][" + CommandCenter.this.mTimeout + "]");
                        } catch (InterruptedException var2) {
                            CommandCenter.this.print("exec join interrupt［"
                                    + this.iCommand.getName() + "][" + CommandCenter.this.mTimeout + "]");
                        }

                        if (this.iCommand.isAlive()) {
                            CommandCenter.this.print("exec join interrupt isAlive［"
                                    + this.iCommand.getName() + "][" + CommandCenter.this.mTimeout + "]");
                            this.iCommand.interrupt();
                            this.iCommand.doForce();
                        }

                        CommandCenter.this.print("exec finish［" + this.iCommand.getName() + "]");
                        CommandCenter.this.mCenterStat = CommandCenter.CenterState.FREE;
                    }
                }
            }
        };
        this.mCommandExec.start();
    }

    private void force() {
        this.print("force start [" + this.mCommandExec + "]");
        if (this.mCommandExec != null) {
            try {
                this.mCommandExec.interrupt();
                this.print("force call interrupt ");
                this.mCommandExec.join();
                this.mCommandExec = null;
            } catch (InterruptedException var2) {
                this.print("force by interrupt ");
            }
        }

    }

    private void listAddCommand(ICommand command) {
        this.print("listAddCommand:" + command.getName());
        this.mCommandQueue.offer(command);
    }

    private void listAddHeadCommand(ICommand command) {
        this.print("listAddHeadCommand:" + command.getName());
        LinkedList mTempQueue = new LinkedList();
        mTempQueue.offer(command);
        mTempQueue.addAll(this.mCommandQueue);
        this.mCommandQueue = mTempQueue;
    }

    private void listAddClearCommand(ICommand command) {
        this.print("listAddClearCommand:" + command.getName());
        LinkedList mTempQueue = new LinkedList();
        mTempQueue.offer(command);
        this.mCommandQueue.clear();
        this.mCommandQueue = mTempQueue;
    }

    private boolean listHasCommand() {
        return this.mCommandQueue.size() != 0;
    }

    private ICommand listNextCommandNext() {
        this.print("listCommandNext:" + this.mCommandQueue.size());
        return this.mCommandQueue.size() != 0 ? (ICommand) this.mCommandQueue.poll() : null;
    }

    private void print(String msg) {
        Log.i("atlas", "CommandCenter"  + msg);
    }

    public static enum CenterState {

        BUSY("BUSY", 0),
        FREE("FREE", 1);
        // $FF: synthetic field
        private static final CommandCenter.CenterState[] ENUM$VALUES = new CommandCenter.CenterState[]{BUSY, FREE};


        private CenterState(String var1, int var2) {
        }
    }
}
