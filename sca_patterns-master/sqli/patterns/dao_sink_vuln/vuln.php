<?php
require_once("./dao.php");
require_once("./init.php");
$db = new PDO('sqlite::memory:', null, null, array(PDO::ATTR_PERSISTENT => true));

$tainted = $_GET['val'];

$values = array();
$values['val'] = $tainted;
$dao = new DAO();
$dao->where($values);
$results = $dao->select();

foreach ($results as $row) 
{
    echo $row['val'];
}


?>