<?php

require_once("./init.php");
$db = new PDO('sqlite::memory:', null, null, array(PDO::ATTR_PERSISTENT => true));

$arr = $_GET['arr'];

for( $i = 0;$i < 2;$i++ ) 
{
    $t_drop = false;
    if( !in_array( $arr[$i], array( "id", "val" ) ) ) {
        $t_drop = true;
    }

    if($t_drop)
    {
        unset($arr[$i]);
    }
}


$sql = 'SELECT '.implode(',', $arr).' FROM Tests WHERE id=1'; 

$results = $db->query($sql);

foreach ($results as $row) 
{
    echo $row['val'];
}


?>