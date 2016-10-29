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
  `REJECTED` varchar(1) DEFAULT NULL,
  `CREDIT_CHECK_FAILED` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `COUNTERPARTY` (
  `ID` varchar(100) NOT NULL,
  `CREDIT_LIMIT` decimal(10,2) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DELIMITER $$
CREATE DEFINER=`root`@`localhost` PROCEDURE `addOrder`(pId varchar(50), pSymbol varchar(5), pQuantity int, pSide varchar(4), pType varchar(20), pTimeInForce varchar(3),
	pLimitPrice decimal(10, 2), pStopPrice decimal(10, 2), pPrice decimal(10, 2), pRejected varchar(1), pCreditCheckFailed varchar(1))
BEGIN
	DECLARE isPresent int default null;
	select max(1) into isPresent 
    from `ORDER` where id = pId;
    if (isPresent is null) then
		insert into `ORDER`(id, symbol, quantity, side, type, time_in_force, limit_price, price, fill_date, rejected, credit_check_failed)
        values (pId, pSymbol, pQuantity, pSide, pType, pTimeInForce, pLimitPrice, pPrice, NOW(), pRejected, pCreditCheckFailed);
    end if;
END$$
DELIMITER ;


DELIMITER $$
CREATE DEFINER=`root`@`localhost` PROCEDURE `getOrders`(pOrderType varchar(20))
BEGIN
	if (pOrderType is null) then
		select * from `ORDER`;
	else
		select * from `ORDER`  where type = pOrderType;
	end if;
END$$
DELIMITER ;

DELIMITER $$
CREATE DEFINER=`root`@`localhost` PROCEDURE `setCredit`(pCounterpartyId varchar(100), pCredit decimal(10, 2))
BEGIN
	update COUNTERPARTY c
    set credit_limit = c.credit_limit + pCredit
    where c.id = pCounterpartyId;
    commit;
END$$
DELIMITER ;

DELIMITER $$
CREATE DEFINER=`root`@`localhost` FUNCTION `hasEnoughCredit`(pCounterpartyId varchar(100), pCredit decimal(10, 2)) RETURNS tinyint(1)
BEGIN
DECLARE result decimal(10,2);
select credit_limit - pCredit into result 
from COUNTERPARTY where id = pCounterpartyId;
if (result > 0) then
	return 1;
else
	return 0;
end if;
END$$
DELIMITER ;


