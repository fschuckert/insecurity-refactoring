<?php

function sink($qry)
{
    $db = new PDO('sqlite::memory:', null, null, array(PDO::ATTR_PERSISTENT => true));
    
    return $db->query($qry);
}

?>