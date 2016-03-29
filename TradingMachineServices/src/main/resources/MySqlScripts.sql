CREATE TABLE `ORDER` (
  `ID` varchar(50) NOT NULL,
  `SYMBOL` varchar(5) NOT NULL,
  `QUANTITY` int(11) NOT NULL,
  `SIDE` varchar(4) NOT NULL,
  `TYPE` varchar(20) NOT NULL,
  `TIME_IN_FORCE` varchar(3) NOT NULL,
  `LIMIT_PRICE` decimal(10,2) DEFAULT NULL,
  `STOP_PRICE` decimal(10,2) DEFAULT NULL,
  `PRICE` decimal(10,2) DEFAULT NULL,
  `ORIGINAL_ID` int(11) DEFAULT NULL,
  `FILL_DATE` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DELIMITER $$
CREATE DEFINER=`root`@`localhost` PROCEDURE `addOrder`(pId varchar(50), pSymbol varchar(5), pQuantity int, pSide varchar(4), pType varchar(20), pTimeInForce varchar(3),
	pLimitPrice decimal(10, 2), pStopPrice decimal(10, 2), pPrice decimal(10, 2))
BEGIN
	DECLARE isPresent int default null;
	select max(1) into isPresent 
    from `ORDER` where id = pId;
    if (isPresent is null) then
		insert into `ORDER`(id, symbol, quantity, side, type, time_in_force, limit_price, price, fill_date)
        values (pId, pSymbol, pQuantity, pSide, pType, pTimeInForce, pLimitPrice, pPrice, NOW());
    end if;
END$$
DELIMITER ;
