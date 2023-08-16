### How do execute the launch command though a shell?

The command specified in the launch.toml are executed without the use of a shell.
This means that functionality provided by the shell such as wildcards, per session environment variables, and aliases, are not evaluated.
Specifically, the process is launched using a popen-like api with shell=False, see [subprocess](https://docs.rs/subprocess/latest/subprocess/) for information on the differences.

There are several reasons for not launching directly through the shell by default. First, it may simply not be necessary if no functionality form the shell is needed.
In this case launching through the shell simply adds more complexity and reduces transparency.
Secondly, a system may have multiple shells and not all platforms have a consistent way to locate the shell.

If you want to invoke the launch command though the shell, you can specify the shell executable as the first argument as shown below:

```toml
# launch.toml
[command]
windows = [ "powershell.exe", "launch.ps1" ]
linux = [ "/bin/sh","launch.sh"]
macos = ["zsh","launch.sh"]
```

The example shows how a platform specific helper script would be invoked:

```bash
# launch.sh
python3.8 --version         # other steps, logging, etc.
EXPORT FOO=BAR              # set environment variable
python3.8 launch.py $1 $2   # last arguments are --handshake-endpoint and its value
```

### Do I need Python to run my FMU?

No, not in the general case. The FMUs generated by unifmu depend ONLY on the commands specified in the launch.toml file.
In the case of the _python_fmu_ example the command launches python, which is where the confusion may arise.

In addition to this the commandline tool is implemented itself is implemented in Python.
To summarize Python is required to use the tool that generates and packages the FMUs, but it is not required during their execution.

### How can I make my FMU portable?

Suppose that your FMU is written in python and that your launch.toml looks like:

```toml
# other targets
linux = [ "python3", "launch.py" ]
```

Using this command the wrapper try to use the system's python3 executable to launch the FMU by invoking executing the launch.py script.
Naturally, the success of this relies on python3 being in the systems path.

To make the FMU portable you could place a complete python interpreter inside resources folder of the FMU.
Then you can invoke the local interpreter rather than the system interpreter, by modifying the `launch.toml` file:

```toml
# other targets
linux = [ "./interpreter_linux/python3", "launch.py" ]
```

This approach is applicable to any OS and runtime dependency.
For Python getting a complete interpreter is a bit trickier, but tools for bundling interpreters and libraries exist such as [PyInstaller](https://pyinstaller.readthedocs.io/en/v4.1/index.html).

### Does an FMU need to support every feature of FMI?

No, the FMI2 specification allows you set falgs that declare the capabilities of an FMU.

For example, you may declare that the FMU supports serialization by setting `canGetAndSetFMUstate` and `canSerializeFMUstate` attributes in the modelDescription.xml, see specification p.25 for more info.
The simulation tool should check these flags during simulation and ensure that only supported operations are executed.

Naturally, the capabilities declared in the model description should also be implemented by the FMU.
The specifics of this depends on the particular backend being used.
For example, using the python backend implementing the capabilities `canGetAndSetFMUstate` and `canSerializeFMUstate` requires that the 2 following methods are defined:

```python
def serialize(self):

    bytes = pickle.dumps(
        (
            self.real_a
            # other attributes
        )
    )
    return bytes, Fmi2Status.ok

def deserialize(self, bytes) -> int:
    (
        real_a
        # other attributes
    ) = pickle.loads(bytes)
    self.real_a = real_a

    return Fmi2Status.ok
```

## Building and Testing

Build the cross compilation image from the dockerfile stored in `docker-build` folder:

```
docker build -t unifmu-build docker-build
```

**Note: This process may take a long time 10-30 minutes, but must only be done once.**

Start a container with the name `builder` from the cross-compilation image `unifmu-build`:

```bash
docker run --name builder -it -v $(pwd):/workdir unifmu-build  # bash
```

```powershell
$pwd = (pwd).Path
docker run --name builder -it -v ${pwd}:/workdir unifmu-build   # powershell
```

**Note: On windows you may have to enable the use of shared folders through the dockers interface, otherwise the container fails to start.**

To build the code invoke the script `docker-build/build_all.sh` in the `workdir` of the container:

```bash
bash ./docker-build/build_all.sh
```

This generates and copies all relevant build artifacts into the `assets/auto_generated` directory:

```
📦auto_generated
 ┣ 📜.gitkeep
 ┣ 📜unifmu.dll
 ┣ 📜unifmu.dylib
 ┣ 📜unifmu.so
 ┣ 📜UnifmuFmi2.cs
 ┗ 📜unifmu_fmi2_pb2.py
```

**Note: On windows Git may be configured to replace LF line-endings with CRLF, which are not compatible with bash.**

Following this the cli is compiled for each platform, including the assets that were just compiled.
The final standalone executables can be found in the target folder, under the host tripple:

- linux: unifmu-x86_64-unknown-linux-gnu-0.0.4.zip
- windows: unifmu-x86_64-pc-windows-gnu-0.0.4.zip
- macOS: unifmu-x86_64-apple-darwin-0.0.4.zip

## Environment Variables

In addition to the systems environment variables, UniFMU defines the following variables in the process created during instantiation of a slave.
These can be accessed during execution by the model implementation or the backend.

| Variable                        | Description                                                                                                                   | Example                               |
| ------------------------------- | ----------------------------------------------------------------------------------------------------------------------------- | ------------------------------------- |
| UNIFMU_GUID                     | The global unique identifier, passed as an argument to fmi2Instantiate                                                        | 77236337-210e-4e9c-8f2c-c1a0677db21b  |
| UNIFMU_INSTANCE_NAME            | Name of the slave instance, passed as an argument to fmi2Instantiate                                                          | left_wheel_motor                      |
| UNIFMU_VISIBLE                  | Flag used to indicating if the instance should run in visible mode, passed as an argument to fmi2Instantiate                  | {true, false}                         |
| UNIFMU_LOGGING_ON               | Flag used to indicating if the instance should run with logging, passed as an argument to fmi2Instantiate                     | {true, false}                         |
| UNIFMU_FMU_TYPE                 | Flag used to indicating if the instance is running in co-sim or model exchange mode, passed as an argument to fmi2Instantiate | {fmi2ModelExchange, fmi2CoSimulation} |
| UNIFMU_DISPATCHER_ENDPOINT      | Endpoint bound by the zmq socket of the binary                                                                                | tcp://127.0.0.1/5000                  |
| UNIFMU_DISPATCHER_ENDPOINT_PORT | Port component of UNIFMU_DISPATCHER_ENDPOINT                                                                                  | 5000                                  |