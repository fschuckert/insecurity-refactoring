<?php

class Url
{
    public function param($param)
    {
        return $_GET[$param];
    }
}

?>