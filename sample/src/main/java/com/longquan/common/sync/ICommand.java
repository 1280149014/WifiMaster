package com.longquan.common.sync;

/**
 * author : charile yuan
 * date   : 21-3-8
 * desc   :
 */
public abstract class ICommand extends Thread {

    public ICommand(String name) {
        super(name);
    }

    @Override
    public void run() {
        this.doWork();
    }

    public abstract void doWork();

    public abstract void doForce();
}
