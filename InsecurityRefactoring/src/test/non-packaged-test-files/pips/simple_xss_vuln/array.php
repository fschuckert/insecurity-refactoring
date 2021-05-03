<?php

$tainted = $_GET['tainted'];

$t = "Hello" . "Bla";


$array = array();

$array[2] = $tainted;

$t .= $array[2];

$t = htmlspecialchars($t, ENT_QUOTES);

echo ($t);



?>
