<?php

$v2 = $_GET['tainted'];
$v3 = $v2;
$b0 = htmlspecialchars($v3);
echo("<script>alert('$b0');</script>");



?>
