package com.longquan.common.wifiap;

import android.net.ProxyInfo;
import android.net.Uri;
import android.os.SystemClock;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * author : charile yuan
 * date   : 21-3-10
 * desc   :
 */
public class WifiAPManager {


    private static final String DEFAULT_HTTPS_URL     = "https://www.google.com/generate_204";
    private static final String DEFAULT_HTTP_URL      =
            "http://connectivitycheck.gstatic.com/generate_204";
    private static final String DEFAULT_FALLBACK_URL  = "http://www.google.com/gen_204";
    private static final String DEFAULT_OTHER_FALLBACK_URLS =
            "http://play.googleapis.com/generate_204";
    private static final String DEFAULT_USER_AGENT    = "Mozilla/5.0 (X11; Linux x86_64) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) "
            + "Chrome/60.0.3112.32 Safari/537.36";

    private static final int SOCKET_TIMEOUT_MS = 10000;
    private static final int PROBE_TIMEOUT_MS  = 3000;

//    protected CaptivePortalProbeResult isCaptivePortal() {
////        if (!mIsCaptivePortalCheckEnabled) {
////            validationLog("Validation disabled.");
////            return CaptivePortalProbeResult.SUCCESS;
////        }
//
//        URL pacUrl = null;
//        URL httpsUrl = makeURL(DEFAULT_HTTPS_URL);
//        URL httpUrl = makeURL(DEFAULT_HTTP_URL);
//
//        // On networks with a PAC instead of fetching a URL that should result in a 204
//        // response, we instead simply fetch the PAC script.  This is done for a few reasons:
//        // 1. At present our PAC code does not yet handle multiple PACs on multiple networks
//        //    until something like https://android-review.googlesource.com/#/c/115180/ lands.
//        //    Network.openConnection() will ignore network-specific PACs and instead fetch
//        //    using NO_PROXY.  If a PAC is in place, the only fetch we know will succeed with
//        //    NO_PROXY is the fetch of the PAC itself.
//        // 2. To proxy the generate_204 fetch through a PAC would require a number of things
//        //    happen before the fetch can commence, namely:
//        //        a) the PAC script be fetched
//        //        b) a PAC script resolver service be fired up and resolve the captive portal
//        //           server.
//        //    Network validation could be delayed until these prerequisities are satisifed or
//        //    could simply be left to race them.  Neither is an optimal solution.
//        // 3. PAC scripts are sometimes used to block or restrict Internet access and may in
//        //    fact block fetching of the generate_204 URL which would lead to false negative
//        //    results for network validation.
//        final ProxyInfo proxyInfo = mNetworkAgentInfo.linkProperties.getHttpProxy();
//        if (proxyInfo != null && !Uri.EMPTY.equals(proxyInfo.getPacFileUrl())) {
//            pacUrl = makeURL(proxyInfo.getPacFileUrl().toString());
//            if (pacUrl == null) {
//                return CaptivePortalProbeResult.FAILED;
//            }
//        }
//
//        if ((pacUrl == null) && (httpUrl == null || httpsUrl == null)) {
//            return CaptivePortalProbeResult.FAILED;
//        }
//
//        long startTime = SystemClock.elapsedRealtime();
//
//        final CaptivePortalProbeResult result;
//        if (pacUrl != null) {
//            result = sendDnsAndHttpProbes(null, pacUrl, ValidationProbeEvent.PROBE_PAC);
//        } else if (mUseHttps) {
//            result = sendParallelHttpProbes(proxyInfo, httpsUrl, httpUrl);
//        } else {
//            result = sendDnsAndHttpProbes(proxyInfo, httpUrl, ValidationProbeEvent.PROBE_HTTP);
//        }
//
//        long endTime = SystemClock.elapsedRealtime();
//
//        sendNetworkConditionsBroadcast(true /* response received */,
//                result.isPortal() /* isCaptivePortal */,
//                startTime, endTime);
//
//        return result;
//    }

    private URL makeURL(String url) {
        if (url != null) {
            try {
                return new URL(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public static final class CaptivePortalProbeResult {
        static final int SUCCESS_CODE = 204;
        static final int FAILED_CODE = 599;

        static final CaptivePortalProbeResult FAILED = new CaptivePortalProbeResult(FAILED_CODE);
        static final CaptivePortalProbeResult SUCCESS = new CaptivePortalProbeResult(SUCCESS_CODE);

        private final int mHttpResponseCode;  // HTTP response code returned from Internet probe.
        final String redirectUrl;             // Redirect destination returned from Internet probe.
        final String detectUrl;               // URL where a 204 response code indicates
        // captive portal has been appeased.

        public CaptivePortalProbeResult(
                int httpResponseCode, String redirectUrl, String detectUrl) {
            mHttpResponseCode = httpResponseCode;
            this.redirectUrl = redirectUrl;
            this.detectUrl = detectUrl;
        }

        public CaptivePortalProbeResult(int httpResponseCode) {
            this(httpResponseCode, null, null);
        }

        boolean isSuccessful() {
            return mHttpResponseCode == SUCCESS_CODE;
        }

        boolean isPortal() {
            return !isSuccessful() && (mHttpResponseCode >= 200) && (mHttpResponseCode <= 399);
        }

        boolean isFailed() {
            return !isSuccessful() && !isPortal();
        }
    }
}
