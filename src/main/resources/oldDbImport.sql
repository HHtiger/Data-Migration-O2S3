CREATE TABLE `oldUser` (
  `id` int  primary key auto_increment,
  `username` varchar(10) DEFAULT NULL,
  `password` varchar(10) DEFAULT NULL,
  `pic` blob DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

insert into oldUser (username, password) values ('oldTiger','123');