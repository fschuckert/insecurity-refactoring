<?php

function sanitize($string)
{
    static $search  = array("\x00", '%',   "'",   '\"');
    static $replace = array('%00',  '%25', "''", '\\\"');

    return str_replace($search, $replace, $string);
}

?>