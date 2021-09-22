<?php

$tainted = $_GET['tainted'];

$tainted = preg_replace("/[^a-zA-Z]/", "", $tainted);

echo $tainted;

?>