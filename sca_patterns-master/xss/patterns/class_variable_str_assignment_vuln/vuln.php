<?php
require_once('classassignment.php');

$foo = new Foo();

$foo->setParam('variable', $_GET['input']);




echo($foo->getParam());

?>