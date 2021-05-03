<?php

include 'ex.php';

$tainted = $_GET['tainted'];

$t2 = $tainted;

add('hello', $t2);


?>
