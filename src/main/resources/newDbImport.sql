CREATE TABLE `newUser` (
  `id` int  primary key auto_increment,
  `username` varchar(10) DEFAULT NULL,
  `password` varchar(10) DEFAULT NULL,
  `status` varchar(1000) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

insert into newUser (username, password) values ('newTiger','123');