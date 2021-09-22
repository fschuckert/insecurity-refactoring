<?php


class myapi
{
    public function mcifileget( $id )
    {
        $retval = array();
        require_once('./init.php');
        $db = new PDO('sqlite::memory:', null, null, array(PDO::ATTR_PERSISTENT => true));
        $sql = "SELECT val FROM Tests WHERE id=$id";
        error_log("got call $sql");
        $results = $db->query($sql);

        foreach ($results as $row) 
        {
            array_push($retval, $row['val']);
        }

        return $retval;
    }
}

$server = new SOAPServer( null, array(
    'uri' => 'http://localhost/test',
));
$server->setClass('myapi');
$server->handle();

echo $retval;


?>