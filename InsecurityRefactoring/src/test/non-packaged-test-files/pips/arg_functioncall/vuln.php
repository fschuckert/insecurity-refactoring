<?php

include 'ex.php';

$tainted = $_GET['tainted'];

$untainted = "Hello";

$t2 = $tainted;

add(source(), $t2);


?>
