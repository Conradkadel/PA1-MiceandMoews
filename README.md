# Endicott CSC460 Search Codebase

This folder contains the following subfolders:

    mazes/       -- holds all the mazes see "Maze file format" below for the format
        (several example maze files)

    python/      -- Python 3 implementation
        (several python files)

    java/        -- Java implementation
        bin/     -- folder for compiled Java class files and images
        csc460/
            (several java files and directories)

Both codebases contain programs to load a maze file from disk and find a path
for the agent to take from a starting spot to the exit using on of several
search algorithms.

## Maze file format

Mazes are defined using the following characters:

    w -- wall; the agent may not pass through these spots
    s -- the start of the maze (entry point)
    e -- the end of the maze (exit point)
      (space) -- an open spot that the agent may freely move into


## Python

To run the Python version, you will need Python 3 installed along with the
`pygame` library (install with the command: `python3 -m pip install pygame`). If
you have multiple versions of Python installed, replace `python3` with the
version you want to use, e.g., that might be `python3.10`. Use `python -V` to
find out what version of Python you're using. To run, do something like:

    cd python
    python3 search_driver.py -p=maze -f=../mazes/maze01.txt -s=bfs

To see all the options, do:

    python3 search_driver.py


## Java

This section assumes you're using a Bash-like terminal to interact with Java; if
you are on Windows, please install GitBash and use that if you don't already
have it (WSL is also fine). If you choose to use an IDE, you'll need to compile
and run according to that IDE.

First, navigate to the `java/` folder:

    cd java

Then compile:

    javac -d bin csc460/*.java csc460/*/*.java

To run, do:

    java -cp bin csc460.drivers.SearchDriver -p=maze -s=bfs -f=../mazes/maze01.txt


To see a list of all options, do:

    java -cp bin csc460.drivers.SearchDriver

If you are running out of heap space, try increasing it. You can set the maximum
heapspace using the `-Xmx<size>` command line argument, where `<size>` is
replaced with a value. The default units are bytes, but you can use a `m` or
`g` suffix for MB and GB, respectively. If I want to use a max heap size of 5 GB
(as in, 5 GB of RAM), I'd run the example above like this:

    java -Xmx5g -cp bin csc460.drivers.SearchDriver -p=maze -s=bfs -f=../mazes/maze01.txt
