<?php

require_once('source.php');

$tainted = $GLOBALS['dbi']->query("SELECT * FROM tables;");

echo $tainted;

?>