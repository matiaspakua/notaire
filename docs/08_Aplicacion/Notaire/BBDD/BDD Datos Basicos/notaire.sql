-- phpMyAdmin SQL Dump
-- version 3.4.10.1deb1
-- http://www.phpmyadmin.net
--
-- Servidor: localhost
-- Tiempo de generación: 19-11-2012 a las 20:57:38
-- Versión del servidor: 5.5.28
-- Versión de PHP: 5.3.10-1ubuntu3.4

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Base de datos: `notaire`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `conceptos`
--

CREATE TABLE IF NOT EXISTS `conceptos` (
  `version` int(11) NOT NULL,
  `id_concepto` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Representa la clave primaria para Conceptos',
  `nombre` text NOT NULL COMMENT 'Indica el nombre del tipo de concepto',
  `valor` float NOT NULL DEFAULT '0' COMMENT 'Indica el valor del concepto, es cero (0) por default.',
  `porcentaje` int(11) NOT NULL DEFAULT '0' COMMENT 'Indica un porcentaje aplicable sobre un valor en particular, es cero (0) por default.',
  `habilitado` tinyint(1) NOT NULL COMMENT 'Indica si este concepto se puede usar o no.',
  `concepto_fijo` tinyint(1) NOT NULL COMMENT 'Indica se el valor del concepto es un valor fijo o calculado en base al porcentaje.',
  PRIMARY KEY (`id_concepto`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 COMMENT='Representa los conceptos (items) que pueden ser aplicados a ' AUTO_INCREMENT=5 ;

--
-- Volcado de datos para la tabla `conceptos`
--

INSERT INTO `conceptos` (`version`, `id_concepto`, `nombre`, `valor`, `porcentaje`, `habilitado`, `concepto_fijo`) VALUES
(1, 1, 'IVA', 0, 20, 1, 0),
(1, 2, 'Honorarios', 0, 5, 1, 0),
(2, 3, 'Documentacion', 500, 0, 1, 1),
(1, 4, 'Protocolo', 150, 0, 1, 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `copias`
--

CREATE TABLE IF NOT EXISTS `copias` (
  `version` int(11) NOT NULL,
  `id_copia` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Representa la clave primaria para Copias (de testimonios)',
  `numero` int(11) NOT NULL COMMENT 'Indica la cantidad de copias que se han generado de un testimonio',
  `fecha_impresion` date NOT NULL COMMENT 'Indica la fecha de impresión de la copia',
  `fecha_retiro` date DEFAULT NULL COMMENT 'Indica la fecha en la cual el cliente ha retirado una copia',
  `observaciones` text COMMENT 'Indica observaciones de la copia, por ejemplo, si fue retirada por otra persona que no sea el cliente.',
  `fk_id_testimonio` int(11) DEFAULT NULL COMMENT 'Clave foránea al testimonio al cual pertenece la copia.',
  `fk_id_persona` int(11) DEFAULT NULL COMMENT 'Clave foránea al cliente al cual pertenece la copia.',
  PRIMARY KEY (`id_copia`),
  KEY `fk_id_testimonio` (`fk_id_testimonio`,`fk_id_persona`),
  KEY `fk_id_persona` (`fk_id_persona`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Tabla que representa las copias generadas para un testimonio' AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `documentos_presentados`
--

CREATE TABLE IF NOT EXISTS `documentos_presentados` (
  `version` int(11) NOT NULL,
  `id_documento_presentado` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Indica la clave primaria para Documentos Presentados',
  `nombre` text NOT NULL COMMENT 'Indica el nombre del tipo de documento (corresponde con los valores de Tipos de Documentos)',
  `numero_carton` int(11) DEFAULT NULL COMMENT 'Indica el número de cartón con el cual fue ingresado',
  `fecha_ingreso` date DEFAULT NULL COMMENT 'Indica la fecha de ingreso a alguna entidad externa',
  `fecha_salida` date DEFAULT NULL COMMENT 'Indica la fecha en que fue devuelto por la entidad externa',
  `preparado` tinyint(1) NOT NULL COMMENT 'Indica si el documento fue preparado o no',
  `vence` tinyint(1) NOT NULL COMMENT 'Indica si el documento vence o no',
  `fecha_vencimiento` date DEFAULT NULL COMMENT 'Si posee vencimiento, indica la fecha del mismo.',
  `dias_vencimiento` int(11) DEFAULT NULL COMMENT 'Si posee vencimiento, indica la cantidad de días de validéz.',
  `importe_a_pagar` float DEFAULT NULL COMMENT 'Indica si para el documento, se debe pagar algún importe.',
  `fecha_pago` date DEFAULT NULL COMMENT 'Si posee importe a pagar, indica la fecha de pago del mismo.',
  `liberado` tinyint(1) NOT NULL COMMENT 'Indica si el documento está liberado o no.',
  `fecha_liberado` date DEFAULT NULL COMMENT 'Indica la fecha de liberación del documento.',
  `observado` tinyint(1) NOT NULL COMMENT 'Indica, si al ser devuelto por la entidad externa, fue observado o no.',
  `observaciones` text COMMENT 'Si fue observado, indica las observaciones del mismo.',
  `entregado` tinyint(1) DEFAULT '0' COMMENT 'Indica si el documento ha sido entregado o no',
  `reingresado` tinyint(1) NOT NULL COMMENT 'Indica si el documento fue reingresado para inscripción.',
  `quien_entrega` text NOT NULL COMMENT 'Indica que tipo de entidad hace entrega del documento: el cliente, o una entidad externa',
  `fk_id_tramite` int(11) NOT NULL COMMENT 'Clave foránea compuesta, hacia gestiones de escrituras',
  `fk_id_tipo_documento` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_documento_presentado`),
  KEY `fk_id_tramite` (`fk_id_tramite`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Tabla que representa el movimiento de los documentos para un' AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `escrituras`
--

CREATE TABLE IF NOT EXISTS `escrituras` (
  `version` int(11) NOT NULL,
  `id_escritura` int(11) NOT NULL AUTO_INCREMENT COMMENT 'indica la clave primaria para Escrituras (clave NO semántica)',
  `numero` int(11) NOT NULL COMMENT 'Clave semántica que representa el número de escritura.',
  `fecha_escrituracion` date NOT NULL COMMENT 'indica la fecha en que se realizó la escritura',
  `cuerpo` text NOT NULL COMMENT 'Detalle completo de la escritura',
  `estado` text NOT NULL COMMENT 'Estado actual de la escritura: Firmada / No Pasó',
  `matricula_inscripcion` text COMMENT 'Indica el número de matricula de inscripción (si corresponde para el tipo de trámite)',
  `fecha_inscripcion` date DEFAULT NULL COMMENT 'Indica la fecha en que se realizó la inscripción (si corresponde)',
  `observaciones` text COMMENT 'Observaciones para la escritura',
  PRIMARY KEY (`id_escritura`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Tabla que representa una Escritura' AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `estados_de_gestion`
--

CREATE TABLE IF NOT EXISTS `estados_de_gestion` (
  `version` int(11) NOT NULL,
  `id_estado_gestion` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Representa la clave primaria para los estados de gestión',
  `nombre` text NOT NULL COMMENT 'Representa el nombre de un estado posible de gestión',
  `observaciones` text COMMENT 'Indica obsevaciones que describen el estado de gestión',
  PRIMARY KEY (`id_estado_gestion`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 COMMENT='Representa los estados por los cuales puede transitar una ge' AUTO_INCREMENT=11 ;

--
-- Volcado de datos para la tabla `estados_de_gestion`
--

INSERT INTO `estados_de_gestion` (`version`, `id_estado_gestion`, `nombre`, `observaciones`) VALUES
(0, 1, 'Iniciada', 'Inicio Gestion'),
(0, 2, 'En Tramite', ''),
(0, 3, 'Documentacion Completa', ''),
(0, 4, 'Archivada', ''),
(0, 5, 'Gestion Modificada', ''),
(0, 6, 'Gestion con Escritura Firmada', ''),
(0, 7, 'Gestion con Escritura Sin Firmar', ''),
(0, 8, 'Gestion con Escritura Anulada', ''),
(0, 9, 'Gestion con Escritura No Paso', ''),
(0, 10, 'Gestion con Escritura Inscripta', '');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `folios`
--

CREATE TABLE IF NOT EXISTS `folios` (
  `version` int(11) NOT NULL,
  `id_folio` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Indica la clave primaria para folios (clave NO semántica)',
  `numero` int(11) NOT NULL COMMENT 'Indica el número de folios (clave semántica)',
  `anio` int(11) NOT NULL COMMENT 'Indica el año al cual corresponde el folio',
  `fk_id_escritura` int(11) DEFAULT NULL COMMENT 'Clave foránea hacia escrituras.',
  `fk_id_tipo_folio` int(11) NOT NULL COMMENT 'Clave foránea hacia copias',
  `fk_id_persona_escribano` int(11) NOT NULL COMMENT 'Clave foránea hacia el Escribano responsable del folio',
  `estado` text NOT NULL COMMENT 'Indica el estado actual del folio',
  `observaciones` text COMMENT 'Indica observaciones en el folio',
  PRIMARY KEY (`id_folio`),
  KEY `fk_id_escritura` (`fk_id_escritura`),
  KEY `fk_id_persona_escribano` (`fk_id_persona_escribano`),
  KEY `fk_id_tipo_folio` (`fk_id_tipo_folio`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Tabla que representa a los folios' AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `folios_copias`
--

CREATE TABLE IF NOT EXISTS `folios_copias` (
  `version` int(11) NOT NULL,
  `fk_id_folio` int(11) NOT NULL,
  `fk_id_copia` int(11) NOT NULL,
  PRIMARY KEY (`fk_id_folio`,`fk_id_copia`),
  KEY `fk_id_folio` (`fk_id_folio`),
  KEY `fk_id_copia` (`fk_id_copia`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `gestiones_de_escrituras`
--

CREATE TABLE IF NOT EXISTS `gestiones_de_escrituras` (
  `version` int(11) NOT NULL,
  `id_gestion` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Representa parte de la clave primaria para ésta relación asociativa',
  `numero` int(11) NOT NULL COMMENT 'Representa el número (clave semantica) de la gestión',
  `fecha_inicio` date NOT NULL COMMENT 'Indica la fecha de inicio de la gestión.',
  `encabezado` text NOT NULL COMMENT 'Indica el titulo/encabezado de la gestión',
  `observaciones` text COMMENT 'Indica observaciones para la gestión.',
  `numero_archivo` int(11) DEFAULT NULL COMMENT 'Indica el número de carpeta en la cual se archiva la gestión',
  `numero_bibliorato` int(11) DEFAULT NULL COMMENT 'Indica el número de bibliorato en el cual se archiva la gestión',
  `fk_id_persona_escribano` int(11) NOT NULL COMMENT 'Representa el escribano asociado a la gestión',
  `fk_id_estado_de_gestion` int(11) DEFAULT NULL COMMENT 'Indica el estado actual de la gestión.',
  PRIMARY KEY (`id_gestion`),
  KEY `fk_id_persona_escribano` (`fk_id_persona_escribano`),
  KEY `fk_id_estado_de_gestion` (`fk_id_estado_de_gestion`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Tabla asociativa que representa la gestión de tramites.' AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `historial`
--

CREATE TABLE IF NOT EXISTS `historial` (
  `version` int(11) NOT NULL,
  `id_historial` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Indica la clave primaria para historial de gestión',
  `fk_id_gestion` int(11) NOT NULL COMMENT 'Clave foránea compuesta hacia gestiones de escrituras',
  `fk_id_estado_gestion` int(11) NOT NULL COMMENT 'Clave foránea hacia estados de gestión',
  `fecha` date NOT NULL COMMENT 'Indica la fecha en que se cambio el estado de la gestión de escrituras.',
  `observaciones` text COMMENT 'Indica observaciones en el cambio de estado.',
  PRIMARY KEY (`id_historial`),
  KEY `fk_id_gestion` (`fk_id_gestion`),
  KEY `fk_id_estado_gestion` (`fk_id_estado_gestion`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Tabla que representa el historial (cambio de estados) de una' AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `inmuebles`
--

CREATE TABLE IF NOT EXISTS `inmuebles` (
  `version` int(11) NOT NULL,
  `id_inmueble` int(11) NOT NULL AUTO_INCREMENT,
  `nomenclatura_catastral` text NOT NULL COMMENT 'Indica el identificador único del inmueble a nivel municipal.',
  `valuacion_fiscal` text COMMENT 'Indica el valor legal del inmueble.',
  `domicilio` text NOT NULL COMMENT 'Representa el domicilio (la dirección física del inmueble)',
  `tipo_inmueble` text NOT NULL COMMENT 'Casa, Terreno, Depto, etc.',
  `observaciones` text COMMENT 'Observaciones que describen el inmueble.',
  PRIMARY KEY (`id_inmueble`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `items`
--

CREATE TABLE IF NOT EXISTS `items` (
  `version` int(11) NOT NULL,
  `id_item` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Indica la clave primaria para Items',
  `nombre` text NOT NULL COMMENT 'Indica el nombre del ítem.',
  `valor` float NOT NULL DEFAULT '0' COMMENT 'Indica el valor de ítem.',
  `porcentaje` int(11) DEFAULT NULL COMMENT 'Indica un valor de procentaje (0-100%)',
  `observaciones` text COMMENT 'Indica observaciones.',
  `fk_id_presupuesto` int(11) NOT NULL COMMENT 'Clave foránea hacia el presupuesto al cual pertenecen los ítems.',
  `concepto_fijo` tinyint(1) NOT NULL COMMENT 'Indica si el item posee un valor fijo o es calculado en base al porcentaje.',
  PRIMARY KEY (`id_item`),
  KEY `fk_id_presupuesto` (`fk_id_presupuesto`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Tabla que representa los ítems de un presupuesto.' AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `movimientos_testimonio`
--

CREATE TABLE IF NOT EXISTS `movimientos_testimonio` (
  `version` int(11) NOT NULL,
  `id_movimiento_testimonio` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Indica la clave primaria para movimiento de testimonios',
  `fecha_ingreso` date NOT NULL COMMENT 'Indica la fecha de ingreso (en la entidad externa)',
  `fecha_salida` date DEFAULT NULL COMMENT 'Indica la fecha de salida (de la entidad externa)',
  `fecha_inscripcion` date DEFAULT NULL COMMENT 'indica la fecha de inscripción',
  `inscripta` tinyint(1) NOT NULL COMMENT 'Indica si fue inscripta o no',
  `numero_carton` int(11) NOT NULL COMMENT 'Indica el número de carton',
  `observaciones` text COMMENT 'Indica las observaciones',
  `fk_id_testimonio` int(11) NOT NULL COMMENT 'Clave foránea hacia Testimonios',
  PRIMARY KEY (`id_movimiento_testimonio`),
  KEY `fk_id_testimonio` (`fk_id_testimonio`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Tabla que representa los movimientos de los testimonios' AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `pagos`
--

CREATE TABLE IF NOT EXISTS `pagos` (
  `version` int(11) NOT NULL,
  `id_pago` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Indica la clave primaria (y número) de pago',
  `monto` float NOT NULL COMMENT 'Indica el monto del pago.',
  `fecha` date NOT NULL COMMENT 'Indica la fecha en que se realizó el pago.',
  `observaciones` text COMMENT 'Indica observaciones del pago realizado.',
  `fk_id_presupuesto` int(11) NOT NULL COMMENT 'Clave foránea hacia el presupuesto, al cual corresponde el pago realizado.',
  PRIMARY KEY (`id_pago`),
  KEY `fk_id_presupuesto` (`fk_id_presupuesto`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Tabla que representa los pagos realizados sobre presupuestos' AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `personas`
--

CREATE TABLE IF NOT EXISTS `personas` (
  `version` int(11) NOT NULL,
  `id_persona` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Representa la clave primaria para la tabla Personas',
  `nombre` text NOT NULL COMMENT 'Indica el nombre de la persona/cliente',
  `apellido` text NOT NULL COMMENT 'Indica el apellido de la persona/cliente',
  `nacionalidad` text COMMENT 'Indica la nacionalidad de la persona/cliente',
  `fk_id_tipo_identificacion` int(11) NOT NULL COMMENT 'Referencia hacia el tipo de identificación de la persona.',
  `numero_identificacion` varchar(20) NOT NULL COMMENT 'Indica el número de identificación',
  `cuit` text COMMENT 'Indica el número de CUIT/CUIL de la persona',
  `sexo` varchar(11) DEFAULT NULL COMMENT 'Indica el sexo de la persona/cliente',
  `fecha_nacimiento` date DEFAULT NULL COMMENT 'Indica la fecha de nacimiento de la persona/cliente',
  `estado_civil` text COMMENT 'Indica el estado civil de la persona/cliente',
  `numero_nupcias` int(11) DEFAULT NULL COMMENT 'Si estuvo casado, indica cuantas nupcias contrajo.',
  `ocupacion` text COMMENT 'Indica la ocupación de la persona/cliente',
  `domicilio` text COMMENT 'Indica el domicilio de la persona/cliente',
  `telefono` text COMMENT 'Indica el número de teléfono de la persona/cliente',
  `e_mail` text COMMENT 'Indica el e-mail de la persona/cliente',
  `registro_escribano` int(11) DEFAULT NULL COMMENT 'Indica si la persona es un escribano.',
  `es_cliente` tinyint(1) NOT NULL COMMENT 'Indica si la persona es cliente o no',
  PRIMARY KEY (`id_persona`),
  KEY `fk_id_tipo_identificacion` (`fk_id_tipo_identificacion`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 COMMENT='Representa a la entidad persona/cliente y en casos particula' AUTO_INCREMENT=3 ;

--
-- Volcado de datos para la tabla `personas`
--

INSERT INTO `personas` (`version`, `id_persona`, `nombre`, `apellido`, `nacionalidad`, `fk_id_tipo_identificacion`, `numero_identificacion`, `cuit`, `sexo`, `fecha_nacimiento`, `estado_civil`, `numero_nupcias`, `ocupacion`, `domicilio`, `telefono`, `e_mail`, `registro_escribano`, `es_cliente`) VALUES
(1, 1, 'Administrador', 'Administrador', NULL, 1, '1234', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0),
(1, 2, 'Gabriela', 'Perez', NULL, 1, '30111222', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '', '', 132, 0);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `plantilla_presupuestos`
--

CREATE TABLE IF NOT EXISTS `plantilla_presupuestos` (
  `version` int(11) NOT NULL,
  `fk_id_tipo_tramite` int(11) NOT NULL COMMENT 'Clave primaria compuesta para plantilla de presupuestos',
  `fk_id_concepto` int(11) NOT NULL COMMENT 'Clave primaria compuesta para plantilla de presupuestos',
  `observaciones` text COMMENT 'Observaciones para plantilla de presupuestos',
  PRIMARY KEY (`fk_id_tipo_tramite`,`fk_id_concepto`),
  KEY `fk_id_concepto` (`fk_id_concepto`),
  KEY `fk_id_tipo_tramite` (`fk_id_tipo_tramite`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Trabla que representa una plantilla de presupuesto';

--
-- Volcado de datos para la tabla `plantilla_presupuestos`
--

INSERT INTO `plantilla_presupuestos` (`version`, `fk_id_tipo_tramite`, `fk_id_concepto`, `observaciones`) VALUES
(0, 1, 1, NULL),
(0, 1, 2, NULL),
(0, 1, 3, NULL),
(0, 1, 4, NULL),
(0, 2, 3, NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `plantilla_tramites`
--

CREATE TABLE IF NOT EXISTS `plantilla_tramites` (
  `version` int(11) NOT NULL,
  `fk_id_tipo_tramite` int(11) NOT NULL COMMENT 'Clave primaria compuesta para plantilla de trámites',
  `fk_id_tipo_documento` int(11) NOT NULL COMMENT 'Clave primaria compuesta para plantilla de trámites',
  `observaciones` text COMMENT 'Observaciones para plantilla de trámites',
  PRIMARY KEY (`fk_id_tipo_tramite`,`fk_id_tipo_documento`),
  KEY `fk_id_tipo_tramite` (`fk_id_tipo_tramite`),
  KEY `fk_id_tipo_documento` (`fk_id_tipo_documento`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Tabla que representa una plantilla de trámite';

--
-- Volcado de datos para la tabla `plantilla_tramites`
--

INSERT INTO `plantilla_tramites` (`version`, `fk_id_tipo_tramite`, `fk_id_tipo_documento`, `observaciones`) VALUES
(0, 1, 2, NULL),
(0, 1, 3, NULL),
(0, 1, 4, NULL),
(0, 2, 2, NULL);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `presupuestos`
--

CREATE TABLE IF NOT EXISTS `presupuestos` (
  `version` int(11) NOT NULL,
  `id_presupuesto` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Indica la clave primaria (y número) del presupuesto.',
  `fecha` date NOT NULL COMMENT 'Indica la fecha en que fue creado el presupuesto',
  `total` float NOT NULL COMMENT 'Indica el monto total del presupuesto, calculado en base a los ítems.',
  `saldo` float NOT NULL DEFAULT '0' COMMENT 'Indica el saldo restante a pagar del presupuesto.',
  `observaciones` text COMMENT 'Indica observaciones sobre el presupuesto.',
  `fk_id_tramite` int(11) NOT NULL COMMENT 'Clave foránea compuesta hacia gestiones de escritura. Inicialmente puede ser nula.',
  `fk_id_persona` int(11) NOT NULL COMMENT 'Clave foránea compuesta hacia gestiones de escritura. Inicialmente puede ser nula.',
  PRIMARY KEY (`id_presupuesto`),
  KEY `fk_id_tramite` (`fk_id_tramite`),
  KEY `fk_id_persona` (`fk_id_persona`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Tabla que representa los presupuestos.' AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `registro_auditoria`
--

CREATE TABLE IF NOT EXISTS `registro_auditoria` (
  `version` int(11) NOT NULL,
  `id_registro_auditoria` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Indica la clave primaria para Registro de Auditoria',
  `fk_id_usuario` int(11) NOT NULL COMMENT 'Clave foránea hacia usuario.',
  `modulo` text NOT NULL COMMENT 'Nombre del modulo donde se está realizando algúna acción',
  `detalle_operacion` text NOT NULL COMMENT 'Detalle de alguna operación realizada.',
  `fecha` datetime NOT NULL COMMENT 'Indica la fecha en la cual se realizó la operación.',
  PRIMARY KEY (`id_registro_auditoria`),
  KEY `fk_id_usuario` (`fk_id_usuario`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 COMMENT='Tabla que mantiene un registro de auditoria de operaciones r' AUTO_INCREMENT=7 ;

--
-- Volcado de datos para la tabla `registro_auditoria`
--

INSERT INTO `registro_auditoria` (`version`, `id_registro_auditoria`, `fk_id_usuario`, `modulo`, `detalle_operacion`, `fecha`) VALUES
(0, 1, 1, 'Ingresar Estado Gestion', 'EstadoDeGestion[ idEstadoGestion=5 ][ nombre=Gestion Modificada ]', '2012-10-23 21:21:53'),
(0, 2, 1, 'Ingresar Estado Gestion', 'EstadoDeGestion[ idEstadoGestion=6 ][ nombre=Gestion con EscrituraFirmada ]', '2012-10-23 21:22:05'),
(0, 3, 1, 'Ingresar Estado Gestion', 'EstadoDeGestion[ idEstadoGestion=7 ][ nombre=Gestion con Escritura Sin Firmar ]', '2012-10-23 21:22:12'),
(0, 4, 1, 'Ingresar Estado Gestion', 'EstadoDeGestion[ idEstadoGestion=8 ][ nombre=Gestion con Escritura Anulada ]', '2012-10-23 21:22:21'),
(0, 5, 1, 'Ingresar Estado Gestion', 'EstadoDeGestion[ idEstadoGestion=9 ][ nombre=Gestion con Escritura No Paso ]', '2012-10-23 21:22:29'),
(0, 6, 1, 'Ingresar Estado Gestion', 'EstadoDeGestion[ idEstadoGestion=10 ][ nombre=Gestion con Escritura Inscripta ]', '2012-10-23 21:22:38');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `suplencias`
--

CREATE TABLE IF NOT EXISTS `suplencias` (
  `version` int(11) NOT NULL,
  `id_suplencia` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Indica la clave primaria para suplencias.',
  `fk_id_suplantado` int(11) NOT NULL COMMENT 'Clave foránea hacia el escribano que se toma licencia (suplantado)',
  `fk_id_suplente` int(11) NOT NULL COMMENT 'Clave foránea hacia el escribano que va a realizar la suplencia (suplente)',
  `fecha_inicio` date NOT NULL COMMENT 'Indica la fecha de inicio de la suplencia.',
  `fecha_fin` date NOT NULL COMMENT 'Indica la fecha de finalización de la suplencia.',
  `observaciones` text COMMENT 'Indica observaciones en la suplencia',
  PRIMARY KEY (`id_suplencia`),
  KEY `fk_id_suplantado` (`fk_id_suplantado`),
  KEY `fk_id_suplente` (`fk_id_suplente`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Tabla que representa cuando un escribano realiza la suplenci' AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `testimonios`
--

CREATE TABLE IF NOT EXISTS `testimonios` (
  `version` int(11) NOT NULL,
  `id_testimonio` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Representa la clave primaria para Testimonios',
  `numero` int(11) NOT NULL COMMENT 'Indica el número del testimonio',
  `observado` tinyint(1) NOT NULL COMMENT 'Indica si fue observado o no',
  `observaciones` text COMMENT 'Si fue observado, indica las observaciones realizadas al testimonio',
  `fk_id_escritura` int(11) NOT NULL COMMENT 'Referencia hacia la escrtura a la cual pertenece el testimonio',
  PRIMARY KEY (`id_testimonio`),
  KEY `fk_id_escritura` (`fk_id_escritura`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Tabla que representa el testimonio (copia) de una escritura' AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `tipos_de_documento`
--

CREATE TABLE IF NOT EXISTS `tipos_de_documento` (
  `version` int(11) NOT NULL,
  `id_tipo_documento` int(11) NOT NULL AUTO_INCREMENT COMMENT 'clave primaria de tipos de documentos',
  `nombre` text NOT NULL COMMENT 'El nombre del tipo de documento',
  `vence` tinyint(1) NOT NULL COMMENT 'indica si posee vencimiento o no.',
  `dias_vencimiento` int(11) DEFAULT NULL COMMENT 'En el caso de poseer vencimiento, indica la cantidad de días antes de vencer.',
  `quien_entrega` text NOT NULL COMMENT 'Indica quién tiene que entregar el documentos: el cliente, o una entidad externa.',
  `habilitado` tinyint(1) NOT NULL COMMENT 'Indica si el tipo de documento esta habilitado para ser utilizado en un plantilla de trámite o no.',
  PRIMARY KEY (`id_tipo_documento`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 COMMENT='Representa los tipos de documentos que se presentan para los' AUTO_INCREMENT=5 ;

--
-- Volcado de datos para la tabla `tipos_de_documento`
--

INSERT INTO `tipos_de_documento` (`version`, `id_tipo_documento`, `nombre`, `vence`, `dias_vencimiento`, `quien_entrega`, `habilitado`) VALUES
(0, 1, 'Certificado Nacimiento', 0, NULL, 'Cliente', 1),
(2, 2, 'Fotocopia Documento Identidad', 0, NULL, 'Cliente', 1),
(1, 3, 'Certificado Dominio', 1, 15, 'Entidad Externa', 1),
(1, 4, 'Certificado Inhibicion', 1, 30, 'Entidad Externa', 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `tipos_de_folio`
--

CREATE TABLE IF NOT EXISTS `tipos_de_folio` (
  `version` int(11) NOT NULL,
  `id_tipo_folio` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Indica la clave primaria para tipos de folios',
  `nombre` text NOT NULL COMMENT 'Indica el nombre del tipo de folio',
  `observaciones` text COMMENT 'Indica observaciones para el tipo de folio',
  `habilitado` tinyint(1) NOT NULL COMMENT 'Indica si el tipo de folio esta habilitado para ser utilizado cuando se registran ingreso de tandas de folios o no.',
  PRIMARY KEY (`id_tipo_folio`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 COMMENT='Tabla que representa los distintos tipos de folios' AUTO_INCREMENT=2 ;

--
-- Volcado de datos para la tabla `tipos_de_folio`
--

INSERT INTO `tipos_de_folio` (`version`, `id_tipo_folio`, `nombre`, `observaciones`, `habilitado`) VALUES
(0, 1, 'Protocolo Principal', '', 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `tipos_de_tramite`
--

CREATE TABLE IF NOT EXISTS `tipos_de_tramite` (
  `version` int(11) NOT NULL,
  `id_tipo_tramite` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Indica la clave primaria del tipo de trámite',
  `nombre` text NOT NULL COMMENT 'Indica el nombre del tipo de trámite',
  `se_archiva` tinyint(1) NOT NULL COMMENT 'Indica si el tipo de trámite requiere ser archivado o no. Si se archiva, se debe registrar el número de carpeta y bibliorato, caso contrario, se registra con cero (0)',
  `se_inscribe` tinyint(1) NOT NULL COMMENT 'Indica si el tipo de trámite debe ser enviado a inscribir, una vez realizada la escritura.',
  `asocia_inmuebles` tinyint(1) NOT NULL COMMENT 'Indica si tipo de trámite actual se le debe asociar un inmueble o no.',
  `observaciones` text COMMENT 'Observaciones que describe el tipo de trámite.',
  `habilitado` tinyint(1) NOT NULL COMMENT 'Indica si el tipo de trámite esta habilitado para poder ser utilizado para iniciar gestiones o no.',
  PRIMARY KEY (`id_tipo_tramite`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 COMMENT='Tabla que representa los tipos de trámites disponibles.' AUTO_INCREMENT=3 ;

--
-- Volcado de datos para la tabla `tipos_de_tramite`
--

INSERT INTO `tipos_de_tramite` (`version`, `id_tipo_tramite`, `nombre`, `se_archiva`, `se_inscribe`, `asocia_inmuebles`, `observaciones`, `habilitado`) VALUES
(7, 1, 'Compraventa', 1, 1, 1, NULL, 1),
(2, 2, 'Certificacion de Firmas', 0, 0, 0, NULL, 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `tipos_identificacion`
--

CREATE TABLE IF NOT EXISTS `tipos_identificacion` (
  `version` int(11) NOT NULL,
  `id_tipo_identificacion` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Clave primaria para tipo de identificación.',
  `nombre` text NOT NULL COMMENT 'Nombre del tipo de identificación.',
  PRIMARY KEY (`id_tipo_identificacion`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=3 ;

--
-- Volcado de datos para la tabla `tipos_identificacion`
--

INSERT INTO `tipos_identificacion` (`version`, `id_tipo_identificacion`, `nombre`) VALUES
(11, 1, 'D.N.I.'),
(2, 2, 'Pasaporte');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `tramites`
--

CREATE TABLE IF NOT EXISTS `tramites` (
  `version` int(11) NOT NULL,
  `id_tramite` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Clave primaria para la tabla tramites.',
  `observaciones` text COMMENT 'Obsevaciones para el tramite actual.',
  `fk_id_tipo_tramite` int(11) NOT NULL COMMENT 'Referencia hacia el tipo de tramite que se está realizando.',
  `fk_id_gestion` int(11) DEFAULT NULL COMMENT 'Referencia hacia la gestión a la cual esta asociado el tramite actual.',
  `fk_id_escritura` int(11) DEFAULT NULL COMMENT 'Referencia hacia la escritura generada en base al tramite de gestion actual,',
  `fk_id_presupuesto` int(11) DEFAULT NULL COMMENT 'Referencia hacia el presupuesto al que pertenece el tramite de gestion.',
  `fk_id_inmueble` int(11) DEFAULT NULL COMMENT 'Referencia (si es que aplica) hacia el inmueble asociado al tramite de gestion actual.',
  PRIMARY KEY (`id_tramite`),
  KEY `fk_id_tipo_tramite` (`fk_id_tipo_tramite`),
  KEY `fk_id_gestion` (`fk_id_gestion`),
  KEY `fk_id_escritura` (`fk_id_escritura`),
  KEY `fk_id_presupuesto` (`fk_id_presupuesto`),
  KEY `fk_id_inmueble` (`fk_id_inmueble`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Representa un trámite de gestión concreto' AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `tramites_personas`
--

CREATE TABLE IF NOT EXISTS `tramites_personas` (
  `version` int(11) NOT NULL,
  `fk_id_tramite` int(11) NOT NULL COMMENT 'Referncia hacia un tramite en curso.',
  `fk_id_persona_cliente` int(11) NOT NULL COMMENT 'Referencia hacia una persona involucrada en un tramite en curso.',
  `observaciones` text COMMENT 'Detalle sobre el registro actual.',
  PRIMARY KEY (`fk_id_tramite`,`fk_id_persona_cliente`),
  KEY `fk_id_tramite` (`fk_id_tramite`),
  KEY `fk_id_persona_cliente` (`fk_id_persona_cliente`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Indica las personas asociadas a un tramite en curso.';

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios`
--

CREATE TABLE IF NOT EXISTS `usuarios` (
  `version` int(11) NOT NULL,
  `id_usuario` int(11) NOT NULL AUTO_INCREMENT COMMENT 'Representa la clave primaria de usuario',
  `nombre` text NOT NULL COMMENT 'Indica el nombre de usuario para ingresar al sistema',
  `contrasenia` text NOT NULL COMMENT 'Indica la contraseña de usuario para ingresar al sistema',
  `estado` tinyint(1) NOT NULL COMMENT 'Indica si está activo o inactivo',
  `tipo` text NOT NULL COMMENT 'Representa el tipo de usuario, y por ende los permisos asociados.',
  `fk_id_persona` int(11) NOT NULL COMMENT 'Clave foránea a los datos del usuario real.',
  PRIMARY KEY (`id_usuario`),
  KEY `fk_id_persona` (`fk_id_persona`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 COMMENT='Tabla que representa a los usuarios del sistema' AUTO_INCREMENT=2 ;

--
-- Volcado de datos para la tabla `usuarios`
--

INSERT INTO `usuarios` (`version`, `id_usuario`, `nombre`, `contrasenia`, `estado`, `tipo`, `fk_id_persona`) VALUES
(7, 1, 'root', '21232f297a57a5a743894a0e4a801fc3', 1, 'Admin', 1);

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `copias`
--
ALTER TABLE `copias`
  ADD CONSTRAINT `copias_ibfk_1` FOREIGN KEY (`fk_id_testimonio`) REFERENCES `testimonios` (`id_testimonio`),
  ADD CONSTRAINT `copias_ibfk_2` FOREIGN KEY (`fk_id_persona`) REFERENCES `personas` (`id_persona`);

--
-- Filtros para la tabla `documentos_presentados`
--
ALTER TABLE `documentos_presentados`
  ADD CONSTRAINT `documentos_presentados_ibfk_5` FOREIGN KEY (`fk_id_tramite`) REFERENCES `tramites` (`id_tramite`);

--
-- Filtros para la tabla `folios`
--
ALTER TABLE `folios`
  ADD CONSTRAINT `folios_ibfk_1` FOREIGN KEY (`fk_id_escritura`) REFERENCES `escrituras` (`id_escritura`),
  ADD CONSTRAINT `folios_ibfk_2` FOREIGN KEY (`fk_id_tipo_folio`) REFERENCES `tipos_de_folio` (`id_tipo_folio`),
  ADD CONSTRAINT `folios_ibfk_3` FOREIGN KEY (`fk_id_persona_escribano`) REFERENCES `personas` (`id_persona`);

--
-- Filtros para la tabla `folios_copias`
--
ALTER TABLE `folios_copias`
  ADD CONSTRAINT `folios_copias_ibfk_1` FOREIGN KEY (`fk_id_folio`) REFERENCES `folios` (`id_folio`),
  ADD CONSTRAINT `folios_copias_ibfk_2` FOREIGN KEY (`fk_id_copia`) REFERENCES `copias` (`id_copia`);

--
-- Filtros para la tabla `gestiones_de_escrituras`
--
ALTER TABLE `gestiones_de_escrituras`
  ADD CONSTRAINT `gestiones_de_escrituras_ibfk_1` FOREIGN KEY (`fk_id_persona_escribano`) REFERENCES `personas` (`id_persona`),
  ADD CONSTRAINT `gestiones_de_escrituras_ibfk_2` FOREIGN KEY (`fk_id_estado_de_gestion`) REFERENCES `estados_de_gestion` (`id_estado_gestion`) ON UPDATE CASCADE;

--
-- Filtros para la tabla `historial`
--
ALTER TABLE `historial`
  ADD CONSTRAINT `historial_ibfk_1` FOREIGN KEY (`fk_id_gestion`) REFERENCES `gestiones_de_escrituras` (`id_gestion`),
  ADD CONSTRAINT `historial_ibfk_2` FOREIGN KEY (`fk_id_estado_gestion`) REFERENCES `estados_de_gestion` (`id_estado_gestion`);

--
-- Filtros para la tabla `items`
--
ALTER TABLE `items`
  ADD CONSTRAINT `items_ibfk_1` FOREIGN KEY (`fk_id_presupuesto`) REFERENCES `presupuestos` (`id_presupuesto`);

--
-- Filtros para la tabla `movimientos_testimonio`
--
ALTER TABLE `movimientos_testimonio`
  ADD CONSTRAINT `movimientos_testimonio_ibfk_1` FOREIGN KEY (`fk_id_testimonio`) REFERENCES `testimonios` (`id_testimonio`);

--
-- Filtros para la tabla `pagos`
--
ALTER TABLE `pagos`
  ADD CONSTRAINT `pagos_ibfk_1` FOREIGN KEY (`fk_id_presupuesto`) REFERENCES `presupuestos` (`id_presupuesto`);

--
-- Filtros para la tabla `personas`
--
ALTER TABLE `personas`
  ADD CONSTRAINT `personas_ibfk_1` FOREIGN KEY (`fk_id_tipo_identificacion`) REFERENCES `tipos_identificacion` (`id_tipo_identificacion`);

--
-- Filtros para la tabla `plantilla_presupuestos`
--
ALTER TABLE `plantilla_presupuestos`
  ADD CONSTRAINT `plantilla_presupuestos_ibfk_1` FOREIGN KEY (`fk_id_tipo_tramite`) REFERENCES `tipos_de_tramite` (`id_tipo_tramite`),
  ADD CONSTRAINT `plantilla_presupuestos_ibfk_2` FOREIGN KEY (`fk_id_concepto`) REFERENCES `conceptos` (`id_concepto`) ON DELETE CASCADE;

--
-- Filtros para la tabla `plantilla_tramites`
--
ALTER TABLE `plantilla_tramites`
  ADD CONSTRAINT `plantilla_tramites_ibfk_3` FOREIGN KEY (`fk_id_tipo_tramite`) REFERENCES `tipos_de_tramite` (`id_tipo_tramite`),
  ADD CONSTRAINT `plantilla_tramites_ibfk_4` FOREIGN KEY (`fk_id_tipo_documento`) REFERENCES `tipos_de_documento` (`id_tipo_documento`) ON DELETE CASCADE;

--
-- Filtros para la tabla `presupuestos`
--
ALTER TABLE `presupuestos`
  ADD CONSTRAINT `presupuestos_ibfk_3` FOREIGN KEY (`fk_id_tramite`) REFERENCES `tramites` (`id_tramite`),
  ADD CONSTRAINT `presupuestos_ibfk_4` FOREIGN KEY (`fk_id_persona`) REFERENCES `personas` (`id_persona`);

--
-- Filtros para la tabla `registro_auditoria`
--
ALTER TABLE `registro_auditoria`
  ADD CONSTRAINT `registro_auditoria_ibfk_1` FOREIGN KEY (`fk_id_usuario`) REFERENCES `usuarios` (`id_usuario`);

--
-- Filtros para la tabla `suplencias`
--
ALTER TABLE `suplencias`
  ADD CONSTRAINT `suplencias_ibfk_1` FOREIGN KEY (`fk_id_suplantado`) REFERENCES `personas` (`id_persona`),
  ADD CONSTRAINT `suplencias_ibfk_2` FOREIGN KEY (`fk_id_suplente`) REFERENCES `personas` (`id_persona`);

--
-- Filtros para la tabla `testimonios`
--
ALTER TABLE `testimonios`
  ADD CONSTRAINT `testimonios_ibfk_1` FOREIGN KEY (`fk_id_escritura`) REFERENCES `escrituras` (`id_escritura`);

--
-- Filtros para la tabla `tramites`
--
ALTER TABLE `tramites`
  ADD CONSTRAINT `tramites_ibfk_1` FOREIGN KEY (`fk_id_tipo_tramite`) REFERENCES `tipos_de_tramite` (`id_tipo_tramite`),
  ADD CONSTRAINT `tramites_ibfk_2` FOREIGN KEY (`fk_id_gestion`) REFERENCES `gestiones_de_escrituras` (`id_gestion`),
  ADD CONSTRAINT `tramites_ibfk_3` FOREIGN KEY (`fk_id_escritura`) REFERENCES `escrituras` (`id_escritura`),
  ADD CONSTRAINT `tramites_ibfk_4` FOREIGN KEY (`fk_id_presupuesto`) REFERENCES `presupuestos` (`id_presupuesto`),
  ADD CONSTRAINT `tramites_ibfk_5` FOREIGN KEY (`fk_id_inmueble`) REFERENCES `inmuebles` (`id_inmueble`);

--
-- Filtros para la tabla `tramites_personas`
--
ALTER TABLE `tramites_personas`
  ADD CONSTRAINT `tramites_personas_ibfk_1` FOREIGN KEY (`fk_id_tramite`) REFERENCES `tramites` (`id_tramite`),
  ADD CONSTRAINT `tramites_personas_ibfk_2` FOREIGN KEY (`fk_id_persona_cliente`) REFERENCES `personas` (`id_persona`);

--
-- Filtros para la tabla `usuarios`
--
ALTER TABLE `usuarios`
  ADD CONSTRAINT `usuarios_ibfk_1` FOREIGN KEY (`fk_id_persona`) REFERENCES `personas` (`id_persona`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
