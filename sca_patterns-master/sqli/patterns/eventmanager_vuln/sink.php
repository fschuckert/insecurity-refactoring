<?php

class Sink
{
    function vuln()
    {
        $db = new PDO('sqlite::memory:', null, null, array(PDO::ATTR_PERSISTENT => true));
        $sql = Event::$data;        

        $results = $db->query($sql);

        foreach ($results as $row) 
        {
            echo $row['val'];
        }
    }
}

Event::add('event', array('sink', 'vuln'));

?>