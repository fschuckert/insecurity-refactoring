<?php ?>
[<?php
$pId = "0";

$pId=$_REQUEST['id'];


$pId = str_replace("%<%", "&lt;", $pId);
$pId = str_replace("%>%", "&gt;", $pId);

echo $pId;

?>]