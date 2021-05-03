## Folder structure

 - sca_patterns_master: I addded simple test, currently the most simple one that fails is test01.php
 - phpjoern-master
 - joern
 - neo4j-community-3.5.13


# Prerequisites


I'm currently using Manjaro, but I have also tried step-by-step installation in a virtual machine with Ubuntu, that's why some packages will be either for Ubuntu or Manjaro. First we need to install:

 - Python (version used: 3.8.1)
 - Java JDK 11
 - Maven
 - php (version used:  PHP 7.4.2 (cli) (built: Jan 21 2020 18:16:58) ( NTS ))
 - php-dev
 - curl
 - Install php-ast (see section php-ast)
 - Install joern + requirements (see section joern)

## php-ast


Now we install the extension following all steps found [here](https://github.com/malteskoruppa/phpjoern#prerequisite-installing-the-php-ast-extension), which are:
```
git clone https://github.com/nikic/php-ast
cd php-ast
phpize
./configure
make
sudo make install
```

Lastly, add the line  `extension=ast.so`  to your  `php.ini`  file. (Ubuntu php7.4 -> /etc/php/7.4/cli/php.ini)

## joern

Latest version of Joern **that had php support**. First we need to install all of the necessary libraries:

1. Install Gradle (4.10.3)
Recommended: install Gradle with [SDK Man](https://sdkman.io/):
```
curl -s "https://get.sdkman.io" | bash
```
Open a new terminal
```
sdk install gradle 4.10.3
```

2. Install graphviz and required python tools
```
sudo apt-get install python3-distutils python3-pip python3-setuptools
pip3 install graphviz
sudo apt-get install libgraphviz-dev pkg-config
```

## Compile joern / joernphp
As last step joern and joernphp are compiled using following command in the base folder of this project:
```
./compile_all
```

# Insecurity Refactoring
The project itself is found in the directory: `{ProjectFolder}/InsecurityRefactoring`

## Compile

It can be compile with following commands:
```
cd InsecurityRefactoring
mvn package
```
## Run the project
A simple script is added to run the project
```
sh run_insec.sh -g
```
The -g parameter starts the user interface.
You can use it from cli to find possible injection points/vulnerabilities. Further information can be found by the -h parameter


# Neo4j 
The code property graph is stored in the provided neo4j database. You can access the database for testing on following url:
http://localhost:7474/


