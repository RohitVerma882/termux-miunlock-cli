package dev.rohitverma882.miunlock.v2.process;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import dev.rohitverma882.miunlock.v2.fastboot.FastbootBinary;

public class FastbootRunner extends ProcessRunner {
    private String deviceSerial = null;

    public FastbootRunner() {
        super(Objects.requireNonNull(FastbootBinary.getFASTBOOT_BINARY()).getPath());
    }

    public void setDeviceSerial(String deviceSerial) {
        this.deviceSerial = deviceSerial;
    }

    @Override
    protected List<String> buildFinalArgumentsList() {
        LinkedList<String> list = new LinkedList<>();
        list.add(executable.toString());
        if (deviceSerial != null) {
            list.add("-s");
            list.add(deviceSerial);
        }
        list.addAll(arguments);
        return list;
    }
}