<?php

include('ex.php');

$tainted = $_GET['tainted'];

$foo = new Foo();

$t3 = $foo->add($tainted, "lala");

echo $t3;



?>
