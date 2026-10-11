package com.licensis.notaire.dto;

/**
 * Classification of a budget item (CU45, CU71).
 * {@code NORMAL} is the default when an item does not specify a type.
 */
public enum TypeItem {
    NORMAL,
    DESCUENTO,
    RECARGO
}
