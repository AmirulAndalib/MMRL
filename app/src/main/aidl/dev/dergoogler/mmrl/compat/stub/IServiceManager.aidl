package dev.dergoogler.mmrl.compat.stub;

import dev.dergoogler.mmrl.compat.stub.IFileManager;
import dev.dergoogler.mmrl.compat.stub.IModuleManager;
import dev.dergoogler.mmrl.compat.stub.IKsuService;

interface IServiceManager {
    int getUid() = 0;
    int getPid() = 1;
    String getSELinuxContext() = 2;
    String currentPlatform() = 3;
    IModuleManager getModuleManager() = 4;
    IFileManager getFileManager() = 5;
    IKsuService getKsuService() = 6;

    void destroy() = 16777114; // Only for Shizuku
}