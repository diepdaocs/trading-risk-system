import { useEffect, useState } from 'react'
import './App.css'

const API_BASE = 'http://localhost:8080/api';

function App() {
  const [instruments, setInstruments] = useState([]);
  const [selectedSymbol, setSelectedSymbol] = useState('AAPL');
  const [orderBook, setOrderBook] = useState({ bids: {}, asks: {} });
  const [trades, setTrades] = useState([]);
  const [riskMetrics, setRiskMetrics] = useState([]);

  // New order form state
  const [orderSide, setOrderSide] = useState('BUY');
  const [orderPrice, setOrderPrice] = useState(0);
  const [orderQuantity, setOrderQuantity] = useState(10);

  // Fetch initial data
  useEffect(() => {
    fetch(`${API_BASE}/instruments`)
      .then(res => res.json())
      .then(data => {
        setInstruments(data);
        if (data.length > 0 && !selectedSymbol) setSelectedSymbol(data[0].symbol);
      })
      .catch(err => console.error("Error fetching instruments:", err));
  }, []);

  // Polling for updates
  useEffect(() => {
    const fetchUpdates = async () => {
      if (!selectedSymbol) return;

      try {
        const [obRes, tradesRes, riskRes] = await Promise.all([
          fetch(`${API_BASE}/orderbook/${selectedSymbol}`),
          fetch(`${API_BASE}/trades?limit=20`),
          fetch(`${API_BASE}/risk`)
        ]);

        if (obRes.ok) setOrderBook(await obRes.json());
        if (tradesRes.ok) setTrades(await tradesRes.json());
        if (riskRes.ok) setRiskMetrics(await riskRes.json());
      } catch (err) {
        console.error("Error fetching updates:", err);
      }
    };

    fetchUpdates();
    const interval = setInterval(fetchUpdates, 1000);
    return () => clearInterval(interval);
  }, [selectedSymbol]);

  const handleOrderSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await fetch(`${API_BASE}/orders`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          symbol: selectedSymbol,
          side: orderSide,
          price: parseFloat(orderPrice),
          quantity: parseInt(orderQuantity)
        })
      });
      if (response.ok) {
        alert('Order submitted successfully!');
      } else {
        alert('Failed to submit order');
      }
    } catch (err) {
      console.error("Error submitting order:", err);
    }
  };

  return (
    <div className="container">
      <header>
        <h1>Trading & Risk System Dashboard</h1>
      </header>

      <div className="main-content">
        <div className="sidebar">
          <h2>Instruments</h2>
          <select
            value={selectedSymbol}
            onChange={(e) => setSelectedSymbol(e.target.value)}
          >
            {instruments.map(inst => (
              <option key={inst.symbol} value={inst.symbol}>
                {inst.symbol} ({inst.assetClass})
              </option>
            ))}
          </select>

          <div className="order-form">
            <h3>New Order</h3>
            <form onSubmit={handleOrderSubmit}>
              <div className="form-group">
                <label>Side:</label>
                <select value={orderSide} onChange={e => setOrderSide(e.target.value)}>
                  <option value="BUY">BUY</option>
                  <option value="SELL">SELL</option>
                </select>
              </div>
              <div className="form-group">
                <label>Price (0 for Market):</label>
                <input
                  type="number"
                  step="0.01"
                  value={orderPrice}
                  onChange={e => setOrderPrice(e.target.value)}
                />
              </div>
              <div className="form-group">
                <label>Quantity:</label>
                <input
                  type="number"
                  value={orderQuantity}
                  onChange={e => setOrderQuantity(e.target.value)}
                  min="1"
                />
              </div>
              <button type="submit" className={`btn ${orderSide.toLowerCase()}`}>
                Submit {orderSide}
              </button>
            </form>
          </div>
        </div>

        <div className="content">
          <div className="row">
            <div className="panel orderbook">
              <h2>Order Book: {selectedSymbol}</h2>
              <div className="book-stats">
                <span>Best Bid: {orderBook?.bestBid?.toFixed(2) || '0.00'}</span>
                <span>Mid: {orderBook?.midPrice?.toFixed(2) || '0.00'}</span>
                <span>Best Ask: {orderBook?.bestAsk?.toFixed(2) || '0.00'}</span>
              </div>
              <div className="book-tables">
                <div className="asks">
                  <h4>Asks</h4>
                  <table>
                    <thead><tr><th>Price</th><th>Qty</th></tr></thead>
                    <tbody>
                      {Object.entries(orderBook?.asks || {})
                        // Sort descending for asks to show best ask at bottom
                        .sort(([p1], [p2]) => parseFloat(p2) - parseFloat(p1))
                        .map(([price, qty]) => (
                        <tr key={price} className="ask-row">
                          <td>{parseFloat(price).toFixed(2)}</td>
                          <td>{qty}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
                <div className="bids">
                  <h4>Bids</h4>
                  <table>
                    <thead><tr><th>Price</th><th>Qty</th></tr></thead>
                    <tbody>
                      {Object.entries(orderBook?.bids || {})
                        .sort(([p1], [p2]) => parseFloat(p2) - parseFloat(p1))
                        .map(([price, qty]) => (
                        <tr key={price} className="bid-row">
                          <td>{parseFloat(price).toFixed(2)}</td>
                          <td>{qty}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>

            <div className="panel trades">
              <h2>Recent Trades</h2>
              <table>
                <thead>
                  <tr>
                    <th>Time</th>
                    <th>Inst</th>
                    <th>Price</th>
                    <th>Qty</th>
                  </tr>
                </thead>
                <tbody>
                  {trades.map(trade => {
                    const time = new Date(trade.timestamp).toLocaleTimeString();
                    return (
                      <tr key={trade.tradeId}>
                        <td>{time}</td>
                        <td>{trade.instrument.symbol}</td>
                        <td>{trade.price.toFixed(2)}</td>
                        <td>{trade.quantity}</td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>

          <div className="panel risk">
            <h2>Risk Dashboard</h2>
            <table>
              <thead>
                <tr>
                  <th>Instrument</th>
                  <th>Position</th>
                  <th>Current Price</th>
                  <th>Unrealized PnL</th>
                  <th>Delta (Eq) / Base Exp (FX)</th>
                  <th>DV01</th>
                </tr>
              </thead>
              <tbody>
                {riskMetrics.map(rm => (
                  <tr key={rm.instrument.symbol}>
                    <td>{rm.instrument.symbol}</td>
                    <td>{rm.netPosition}</td>
                    <td>{rm.currentPrice.toFixed(2)}</td>
                    <td className={rm.unrealizedPnl >= 0 ? 'positive' : 'negative'}>
                      {rm.unrealizedPnl.toFixed(2)}
                    </td>
                    <td>{rm.delta.toFixed(2)}</td>
                    <td>{rm.dv01.toFixed(4)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  )
}

export default App
