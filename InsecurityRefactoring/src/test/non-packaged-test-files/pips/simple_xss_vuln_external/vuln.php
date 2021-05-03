<?php

include 'ex.php';

$tainted = $_GET['tainted'];

$untainted = "Hello";

$t2 = $tainted;

add($_GET['lala'], $t2);


?>
