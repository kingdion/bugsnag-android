package com.bugsnag.android;

/**
 * A compatibility implementation of {@link Delivery} which wraps {@link ErrorReportApiClient} and
 * {@link SessionTrackingApiClient}. This class allows for backwards compatibility for users still
 * utilising the old API, and should be removed in the next major version.
 */
class DeliveryCompat implements Delivery {

    volatile ErrorReportApiClient errorReportApiClient;
    volatile SessionTrackingApiClient sessionTrackingApiClient;

    @Override
    public void deliver(SessionTrackingPayload payload,
                        Configuration config) throws BadResponseException, NetworkException {
        if (sessionTrackingApiClient != null) {
            sessionTrackingApiClient.postSessionTrackingPayload(config.getSessionEndpoint(),
                payload, config.getSessionApiHeaders());
        }
    }

    @Override
    public void deliver(Report report, Configuration config)
        throws BadResponseException, NetworkException {
        if (errorReportApiClient != null) {
            errorReportApiClient.postReport(config.getEndpoint(),
                report, config.getErrorApiHeaders());
        }
    }

}
