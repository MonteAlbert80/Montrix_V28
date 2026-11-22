package com.monte.montrix.model.bluetooth;
/** Immutable, presentation-ready state for the Bluetooth dashboard component.
 * All values are supplied by the corresponding service; this class performs no calculations.
 */
public final class BluetoothModel {
    /** Preformatted text supplied by the service layer. */
    public final String connectionText;
    /** Creates a state snapshot without modifying its data.
     * @param connectionText preformatted service-provided value
     */
    public BluetoothModel(String connectionText) {
        this.connectionText = connectionText == null ? "" : connectionText;
    }
}
