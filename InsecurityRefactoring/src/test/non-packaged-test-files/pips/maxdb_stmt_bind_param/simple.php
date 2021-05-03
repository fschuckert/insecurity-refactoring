<?php


$a = $_GET['lala'];

$b = $a;



maxdb_stmt_bind_param($stmt, 'sss', $zip, $b, $state);
?>
