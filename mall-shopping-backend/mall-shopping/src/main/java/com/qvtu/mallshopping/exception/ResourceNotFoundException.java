package com.qvtu.mallshopping.exception;

public class ResourceNotFoundException extends RuntimeException {
    private String resourceName;
    private String fieldName;
    private Object fieldValue;

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s with %s: %s was not found", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public ResourceNotFoundException(String message) {
        super(message);
        // 从消息中提取资源信息
        this.resourceName = "Resource";
        this.fieldName = "unknown";
        this.fieldValue = "unknown";
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }

    // 添加一个便捷的静态方法来创建异常
    public static ResourceNotFoundException notFound(String resourceType, String id) {
        return new ResourceNotFoundException(resourceType, "id", id);
    }
}
