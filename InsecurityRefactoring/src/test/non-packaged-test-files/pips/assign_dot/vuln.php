<?php

$hello = "Hello";

$tainted = $_GET['tainted'];

$hello .= $tainted;


echo $hello;



?>
