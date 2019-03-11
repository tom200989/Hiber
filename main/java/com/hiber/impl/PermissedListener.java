package com.hiber.impl;

/**
 * 权限监听接口
 */
public interface PermissedListener {
    void permissionResult(boolean isPassAllPermission, String[] denyPermissions);
}
