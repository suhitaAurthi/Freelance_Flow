package com.example.freelanceflowandroid.data;

/**
 * Simple wrapper used by LiveData to represent loading / success / error states.
 * Matches the usages in your activities (resource.status, resource.exception, Resource.success/error/loading()).
 */
public class Resource<T> {
    public enum Status { SUCCESS, ERROR, LOADING }

    public final Status status;
    public final T data;
    public final Exception exception;

    private Resource(Status status, T data, Exception exception) {
        this.status = status;
        this.data = data;
        this.exception = exception;
    }

    public static <T> Resource<T> success(T data) {
        return new Resource<>(Status.SUCCESS, data, null);
    }

    public static <T> Resource<T> error(Exception e) {
        return new Resource<>(Status.ERROR, null, e);
    }

    public static <T> Resource<T> loading() {
        return new Resource<>(Status.LOADING, null, null);
    }

    @Override
    public String toString() {
        return "Resource{status=" + status + ", data=" + data + ", exception=" + exception + "}";
    }
}