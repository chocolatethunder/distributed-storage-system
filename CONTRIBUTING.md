# CONTRIBUTING

Before adding anything to this repository please first read this document. The Gradle generated files might look intimidating but they are not. Just quickly look over this brief document. 

1. [INSTALL BUILDTOOLS](#INSTALL-BUILDTOOLS)
1. [COMMITTING](#COMMITTING)
1. [HOW TO CONTRIBUTE](#HOW-TO-CONTRIBUTE)
1. [HOW TO BUILD](#HOW-TO-BUILD)
1. [RESPONSIBILITIES](#RESPONSIBILITIES)

# INSTALL BUILDTOOLS
Hopefully you are all running Windows 10. 

1. Go to [Chocolatey's website](https://chocolatey.org/) and install Chocolatey.
1. Open Windows Powershell and run it as an administrator
1. Type the command `choco install gradle`
1. Type the command `gradle -v` to confirm installation


# COMMITTING

## Master
This is the master code. Only code that is production ready will go in here. No exceptions.

### When to commit
1. The code has passed all the jUnit tests
2. It has passed the hardware execution on the Raspberry Pi's **[!important]**

### How to commit
1. Merge the branch by using the following commands:
  - ``
git checkout master
``
  - ``
git merge development
``

## Development

This is the branch where you will branch out, add your code/features, make sure they work properly, then merge them into the development branch for testing. 

### When to commit
1. You have coded and tested the feature(s) you want. 
1. After you have communicated with other to make sure there is no conflict.

### How to commit

1. Check to make sure you have the development branch by using the command ``git branch``
  - If you only see the master branch then run the following command `git branch development`. You should see the following message:
    ```
    Branch development set up to track remote branch development from origin. Switched to a new branch 'development'
    ```
  - If you see the development branch and it is not highlighted green then run the following command ``git branch development``

1. Use git command `git branch <your feature name> && git checkout <your feature name>` to create and then checkout into your new branch.

1. Code and test your awesome feature(s)

1. Merge the branch by using the following commands:
  - ``
git checkout development
``
  - ``
git merge <your feature name>
``

# HOW TO CONTRIBUTE
Pay attention to these three project directories.

## library
### *Source*
*\library\src\main\java\app*

You may create new classes in here that are shared amongest the stalker and harm code. Please include *package app;* at the very top when you create a new class.

### *Test*
*\library\src\test\java\app*

Please use jUnit to test out your code by putting it here. Follow the convention <Classname>Test.java. Feel free to copy the contents of LibraryTest.java (Sample file). Don't forget to include *package app;* at the very top when you create a new jUnit test.

## stalker
### *Source*
*\stalker\src\main\java\app*

Create classes in here that are only relevant to the STALKER units. Please include *package app;* at the very top when you create a new class.

### *Test*
*\stalker\src\test\java\app*

Please use jUnit to test out your code by putting it here. Follow the convention <Classname>Test.java. Feel free to copy the contents of AppTest.java (Sample file). Don't forget to include *package app;* at the very top when you create a new jUnit test.

## harm
### *Source*
*\harm\src\main\java\app*

Create classes in here that are only relevant to the HARM targets. Please include *package app;* at the very top when you create a new class.

### *Test*
*\harm\src\test\java\app*

Please use jUnit to test out your code by putting it here. Follow the convention <Classname>Test.java. Feel free to copy the contents of AppTest.java (Sample file). Don't forget to include *package app;* at the very top when you create a new jUnit test.

## jcp
### *Source*
*\jcp\src\main\java\app*

Create classes in here that are only relevant to the Java Client Program. Please include *package app;* at the very top when you create a new class.

### *Test*
*\jcp\src\test\java\app*

Please use jUnit to test out your code by putting it here. Follow the convention <Classname>Test.java. Feel free to copy the contents of AppTest.java (Sample file). Don't forget to include *package app;* at the very top when you create a new jUnit test.

## How to include the shared library
You don't need to. Maven knows the library folder is a dependancy. Simply call any class you create and test inside the *\library\src\main\java\app* folder.

# HOW TO BUILD

## All the projects at the same time
Simply run `gradle build`

## How to compile projects individually
Simply cd into the project (stalker|harm|library) and build.

# RESPONSIBILITIES
- Report bugs asap
- Don't break other people's code
- Use branches to compartmentalize and to make sure you don't mess the whole repo
- Code lots
- Code clean
- Document your code so others know what is going on
- Use descriptive git commit messages