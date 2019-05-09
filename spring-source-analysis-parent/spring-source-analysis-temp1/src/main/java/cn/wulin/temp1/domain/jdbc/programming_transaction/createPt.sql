
CREATE DATABASE test_spring CHARACTER SET 'utf8' COLLATE 'utf8_bin'

create table user(
  id int(11) not null auto_increment,
  name varchar(255) default null,
  age int(11) default null,
  sex varchar(255) default null,
  primary key (id)
) engine=InnoDB DEFAULT CHARSET=utf8;