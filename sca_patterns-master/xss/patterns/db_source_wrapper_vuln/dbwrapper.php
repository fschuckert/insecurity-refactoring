<?php

class DB_Wrapper{
    private $_extension;

    public function __construct(DBInterface $ext)
    {
        $this->_extension = $ext;
    }

    public function query($query) {
        $res = $this->_extension->realQuery($query);
        return $res;
    }

}

?>