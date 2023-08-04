package dev.rohitverma882.miunlock.v2.process;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import dev.rohitverma882.miunlock.v2.utility.RunnableWithArg;
import dev.rohitverma882.miunlock.v2.utility.Thrower;
import dev.rohitverma882.miunlock.v2.utility.WaitSemaphore;

public class ProcessRunner {
    private static int processNumber = 0;
    private final WaitSemaphore readFinishedSemaphore = new WaitSemaphore(0);
    private final List<RunnableWithArg> syncCallbacks = Collections.synchronizedList(new ArrayList<>());
    protected Path executable;
    protected LinkedList<String> arguments;
    protected List<String> outputBuffer = Collections.synchronizedList(new LinkedList<>());
    private int secondsTimeout = 1800;
    private Process runningProcess = null;
    private int exitValue = 0;
    private ProcessStatus status = ProcessStatus.READY;
    private File workingDir;
    private int pNum = -1;
    private Thrower<IOException> IOThrower;

    public ProcessRunner(Path exe) {
        this(exe, null);
    }

    public ProcessRunner(String pathExe, String[] arguments) {
        this(Paths.get(pathExe), arguments);
    }

    public ProcessRunner(String pathExe) {
        this(Paths.get(pathExe));
    }

    public ProcessRunner(Path exe, String[] arguments) {
        this.executable = exe;
        this.arguments = new LinkedList<>();
        if (arguments != null) {
            this.arguments.addAll(Arrays.asList(arguments));
        }
    }

    public void setWorkingDir(File workingDir) {
        this.workingDir = workingDir;
    }

    private void setStatus(ProcessStatus status) {
        this.status = status;
    }

    public Process start() throws IOException {
        List<String> args = buildFinalArgumentsList();
        StringBuilder stringBuilder = new StringBuilder();
        for (String arg : args) {
            stringBuilder.append(" ").append('"').append(arg).append('"');
        }
        processNumber++;
        pNum = processNumber;
        ProcessBuilder builder = new ProcessBuilder(args);
        builder.redirectErrorStream(true);
        builder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        if (this.workingDir != null) {
            builder.directory(this.workingDir);
        }
        Process proc = builder.start();
        IOThrower = new Thrower<>();
        final InputStream inputStream = proc.getInputStream();
        new Thread(() -> {
            try {
                readFinishedSemaphore.setPermits(0);
                Scanner scanner = new Scanner(inputStream);
                scanner.useDelimiter(Pattern.compile("[\\r\\n;]+"));
                String data;
                while (scanner.hasNextLine()) {
                    data = manageLineOutput(scanner.nextLine());
                    if (data == null) {
                        continue;
                    }
                    outputBuffer.add(data);
                    for (RunnableWithArg toDo : syncCallbacks) {
                        toDo.run(data);
                    }
                }
            } catch (Exception ignored) {
            } finally {
                readFinishedSemaphore.increase();
            }
        }).start();
        setStatus(ProcessStatus.RUNNING);
        return runningProcess = proc;
    }

    protected List<String> buildFinalArgumentsList() {
        LinkedList<String> list = new LinkedList<>();
        list.add(executable.toString());
        list.addAll(arguments);
        return list;
    }

    public boolean kill() {
        if (!isAlive()) {
            return false;
        }
        runningProcess.destroy();
        return true;
    }

    public boolean isAlive() {
        return !ProcessStatus.RUNNING.equals(this.status) || runningProcess == null || !runningProcess.isAlive();
    }

    private String manageLineOutput(String line) {
        return line;
    }

    public int runWait(int timeout) throws IOException {
        Process process;
        if (status != ProcessStatus.RUNNING || runningProcess == null) {
            process = start();
        } else {
            process = runningProcess;
        }
        IOThrower.check();
        try {
            if (!process.waitFor(timeout, TimeUnit.SECONDS)) {
                throw new InterruptedException("Process didn't exited before timeout");
            }
            IOThrower.check();
            setStatus(ProcessStatus.FINISHED);
            this.exitValue = process.exitValue();
        } catch (InterruptedException e) {
            this.exitValue = -1;
            setStatus(ProcessStatus.EXCEPTION);
        }
        if (process.isAlive()) {
            process.destroyForcibly();
        }
        return this.exitValue;
    }

    public int runWait() throws IOException {
        return runWait(this.secondsTimeout);
    }

    public void addSyncCallback(RunnableWithArg callback) {
        this.syncCallbacks.add(callback);
    }

    public void addArgument(String arg) {
        this.arguments.addLast(arg);
    }

    public String getOutputString() {
        waitOutputReadFinished();
        return String.join("\n", outputBuffer);
    }

    public List<String> getOutputLines() {
        waitOutputReadFinished();
        return new LinkedList<>(outputBuffer);
    }

    public int getExitValue() {
        return exitValue;
    }

    private boolean waitOutputReadFinished() {
        try {
            readFinishedSemaphore.waitOnce(4);
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }

    enum ProcessStatus {
        READY, RUNNING, FINISHED, EXCEPTION
    }
}
