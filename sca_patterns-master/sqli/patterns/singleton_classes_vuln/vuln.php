<?php
require_once("./activerecord.php");
require_once("./init.php");
$db = new PDO('sqlite::memory:', null, null, array(PDO::ATTR_PERSISTENT => true));



$sql = ActiveRecord::model("ModelClass")->getQueryStr();

$results = $db->query($sql);

foreach ($results as $row) 
{
    echo $row['val'];
}


?>