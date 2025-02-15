package dev.dergoogler.mmrl.compat.stub;

import dev.dergoogler.mmrl.compat.content.LocalModule;

interface IShellCallback {
    void onStdout(String msg);
    void onStderr(String msg);
    void onSuccess(in LocalModule module);
    void onFailure();
}