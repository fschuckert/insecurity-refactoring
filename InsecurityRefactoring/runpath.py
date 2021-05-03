import os
from subprocess import PIPE, Popen, run, check_output, STDOUT

def _cmdline(command):
    result = run(
        args=command,
        stdout=PIPE,
        stderr=PIPE,
        shell=True,
        universal_newlines=True
    )
    return result

    # return check_output(command, stderr=STDOUT, shell=True)

def call_on_path(dir_, command, pipeIntoFile=None):
    """ This functions does a system call. Be careful, no sanitization is happening!
    
    Arguments:
        dir_ {str} -- The path where it will be called
        command {str} -- The command
    
    Keyword Arguments:
        pipeIntoFile {[type]} -- [description] (default: {None})
    
    Returns:
        (Output, Error) -- The output and errors are returned as a tuple
    """

    print("Calling (" +dir_ +"): " + command)
    cur_dir = os.getcwd()
    os.chdir(dir_)
    retval = _cmdline(command)
    #retval = os.system(command)
    os.chdir(cur_dir)

    output = str(retval.stdout)
    error =  str(retval.stderr)
    # print("Output: {o}".format(o=output))
    # print("Error: {e}".format(e=error))

    return output, error


if __name__ == "__main__":
    output, error = call_on_path("/home/blubbomat", "ls; lsl")
    print("here")
    print(output)
    print(error)
