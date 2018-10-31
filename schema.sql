CREATE TABLE IF NOT EXISTS `rates` (
    `id` INT AUTO_INCREMENT,
    `value` DOUBLE NOT NULL,
    `dateLabel` DATE NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=INNODB;

CREATE INDEX `dateLabel_idx` ON `rates` `dateLabel`;