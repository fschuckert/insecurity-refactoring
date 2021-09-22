<?php

/**
 * Contract for every database extension supported by phpMyAdmin
 *
 * @package PhpMyAdmin-DBI
 */
interface DBInterface
{

    public function connect();


    public function realQuery($query);


}
?>