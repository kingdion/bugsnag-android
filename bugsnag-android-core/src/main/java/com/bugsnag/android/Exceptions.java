package com.bugsnag.android;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Unwrap and serialize exception information and any "cause" exceptions.
 */
class Exceptions implements JsonStream.Streamable {

    private final BugsnagException exception;
    private String exceptionType;
    private String[] projectPackages;

    Exceptions(Configuration config, BugsnagException exception) {
        this.exception = exception;
        exceptionType = exception.getType();
        projectPackages = config.getProjectPackages();
    }

    @Override
    public void toStream(@NonNull JsonStream writer) throws IOException {
        writer.beginArray();

        // Unwrap any "cause" exceptions
        Throwable currentEx = exception;
        while (currentEx != null) {
            if (currentEx instanceof JsonStream.Streamable) {
                ((JsonStream.Streamable) currentEx).toStream(writer);
            } else {
                String exceptionName = currentEx.getClass().getName();
                String localizedMessage = currentEx.getLocalizedMessage();
                StackTraceElement[] stackTrace = currentEx.getStackTrace();
                exceptionToStream(writer, exceptionName, localizedMessage, stackTrace);
            }
            currentEx = currentEx.getCause();
        }

        writer.endArray();
    }

    BugsnagException getException() {
        return exception;
    }

    String getExceptionType() {
        return exceptionType;
    }

    void setExceptionType(@NonNull String type) {
        exceptionType = type;
        exception.setType(exceptionType);
    }

    String[] getProjectPackages() {
        return projectPackages;
    }

    void setProjectPackages(String[] projectPackages) {
        this.projectPackages = projectPackages;
        exception.setProjectPackages(projectPackages);
    }

    private void exceptionToStream(@NonNull JsonStream writer,
                                   String name,
                                   String message,
                                   StackTraceElement[] frames) throws IOException {
        writer.beginObject();
        writer.name("errorClass").value(name);
        writer.name("message").value(message);
        writer.name("type").value(exceptionType);

        Stacktrace stacktrace = new Stacktrace(frames, projectPackages);
        writer.name("stacktrace").value(stacktrace);
        writer.endObject();
    }
}
