import React, { useState, useEffect, useRef } from 'react';
import { 
  Activity, 
  Terminal, 
  Brain, 
  Database, 
  ShieldAlert, 
  Clock, 
  CheckCircle2, 
  ArrowDown,
  ChevronDown,
  ChevronUp,
  Server,
  Network
} from 'lucide-react';

const API_BASE = `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/incidents`;

function App() {
  const [history, setHistory] = useState([]);
  const [incidentText, setIncidentText] = useState('');
  
  const [analyzing, setAnalyzing] = useState(false);
  const [analysisResult, setAnalysisResult] = useState(null);
  
  const [resolving, setResolving] = useState(false);
  const [resolveForm, setResolveForm] = useState({ resolution: '', outcome: '', rootCause: '' });
  const [resolved, setResolved] = useState(false);
  const [resolveExpanded, setResolveExpanded] = useState(false);

  const section2Ref = useRef(null);
  const section3Ref = useRef(null);

  useEffect(() => {
    fetchHistory();
  }, []);

  const fetchHistory = async () => {
    try {
      const res = await fetch(`${API_BASE}/history?limit=15`);
      if (res.ok) {
        const data = await res.json();
        if (data.items) setHistory(data.items);
      }
    } catch (e) {
      console.error("Failed to fetch history", e);
    }
  };

  const handleAnalyze = async () => {
    if (!incidentText.trim()) return;
    setAnalyzing(true);
    setAnalysisResult(null);
    setResolved(false);
    
    try {
      const res = await fetch(`${API_BASE}/analyze`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ incident: incidentText })
      });
      
      if (res.ok) {
        const data = await res.json();
        setAnalysisResult(data);
        setResolveExpanded(false);
        setTimeout(() => {
          section2Ref.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }, 100);
      } else {
        alert("Analysis failed. See console.");
      }
    } catch (e) {
      console.error(e);
      alert("Failed to connect to backend");
    } finally {
      setAnalyzing(false);
    }
  };

  const handleResolve = async () => {
    if (!resolveForm.resolution || !resolveForm.outcome) return;
    setResolving(true);
    
    try {
      const res = await fetch(`${API_BASE}/resolve`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          incident: incidentText,
          rootCause: resolveForm.rootCause || (analysisResult?.analysis?.likelyRootCause || ''),
          resolution: resolveForm.resolution,
          outcome: resolveForm.outcome
        })
      });
      
      if (res.ok) {
        setResolved(true);
        fetchHistory(); // Refresh sidebar history
      } else {
        alert("Failed to retain memory.");
      }
    } catch (e) {
      console.error(e);
    } finally {
      setResolving(false);
    }
  };

  const resetFlow = () => {
    setIncidentText('');
    setAnalysisResult(null);
    setResolved(false);
    setResolveForm({ resolution: '', outcome: '', rootCause: '' });
  };

  return (
    <div className="app-container">
      {/* HEADER */}
      <header className="header">
        <div className="header-brand">
          <Activity size={24} className="brand-icon" />
          <span className="brand-title">OpsMemory</span>
          <span className="brand-subtitle">AI Incident Command Center</span>
        </div>
        <div className="system-status">
          <div className="status-dot"></div>
          SYSTEM HEALTHY
        </div>
      </header>

      <div className="main-layout">
        {/* SIDEBAR: HISTORY */}
        <aside className="sidebar">
          <div className="sidebar-header">
            <Database size={16} />
            Hindsight Memory Bank
          </div>
          <div className="history-list">
            {history.map((item) => (
              <div key={item.id} className="history-item">
                <div className="history-meta">
                  <span className="history-id mono">{item.document_id || item.id.substring(0,8)}</span>
                  <span>{new Date(item.date).toLocaleDateString()}</span>
                </div>
                <div className="history-text">{item.text}</div>
              </div>
            ))}
            {history.length === 0 && (
              <div className="text-muted" style={{ fontSize: '0.85rem', textAlign: 'center', marginTop: '2rem' }}>
                No historical incidents found.
              </div>
            )}
          </div>
        </aside>

        {/* MAIN FLOW */}
        <main className="content-area">
          <div className="flow-container">
            
            {/* STAGE 1: INCIDENT REPORT */}
            <div className="panel">
              <div className="panel-header">
                <Terminal size={18} className="text-cyan" />
                <span className="panel-title">1. Current Incident</span>
              </div>
              <div className="panel-body flex-col gap-4">
                <textarea 
                  rows={4}
                  placeholder="Describe the production incident (e.g. SEV-1: Payment service is timing out...)"
                  value={incidentText}
                  onChange={e => setIncidentText(e.target.value)}
                  disabled={analyzing || analysisResult != null}
                />
                
                {!analysisResult && (
                  <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                    <button 
                      className="btn-primary" 
                      onClick={handleAnalyze}
                      disabled={!incidentText.trim() || analyzing}
                    >
                      {analyzing ? (
                        <>Processing...</>
                      ) : (
                        <>
                          <Brain size={18} />
                          Analyze with Hindsight
                        </>
                      )}
                    </button>
                  </div>
                )}
              </div>
            </div>

            {/* STAGE 2: AGENT ANALYSIS */}
            {analysisResult && (
              <>
                <div className="flow-connector">
                  <button 
                    className="nav-arrow-button"
                    onClick={() => section2Ref.current?.scrollIntoView({ behavior: 'smooth', block: 'start' })}
                    title="Scroll to OpsMemory Intelligence"
                  >
                    <ArrowDown size={24} />
                  </button>
                </div>
                
                <div className="panel" ref={section2Ref}>
                  <div className="panel-header" style={{ borderBottomColor: analysisResult.analysis.severity.includes('SEV-1') ? 'var(--severity-red)' : 'var(--border-subtle)' }}>
                    <Brain size={18} className="text-cyan" />
                    <span className="panel-title">2. OpsMemory Intelligence</span>
                  </div>
                  <div className="panel-body analysis-grid">
                    
                    {/* Left: AI Reasoning */}
                    <div className="analysis-section">
                      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1rem' }}>
                        <div className="severity-badge">{analysisResult.analysis.severity}</div>
                        <h3 style={{ margin: 0, fontSize: '1.1rem' }}>Analysis & Actions</h3>
                      </div>
                      
                      <div className="data-card">
                        <div className="data-card-label">Likely Root Cause</div>
                        <div className="data-card-value">{analysisResult.analysis.likelyRootCause}</div>
                      </div>

                      <div className="data-card">
                        <div className="data-card-label">Recommended Immediate Actions</div>
                        <ul className="action-list">
                          {analysisResult.analysis.immediateActions.map((action, i) => (
                            <li key={i}>{action}</li>
                          ))}
                        </ul>
                      </div>

                      <div className="data-card">
                        <div className="data-card-label">Agent Reasoning</div>
                        <div className="data-card-value" style={{ fontStyle: 'italic', color: 'var(--text-secondary)' }}>
                          "{analysisResult.analysis.reasoning}"
                        </div>
                      </div>
                    </div>

                    {/* Right: Hindsight Memory */}
                    <div className="analysis-section">
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem', color: 'var(--accent-cyan)' }}>
                        <Database size={18} />
                        <h3 style={{ margin: 0, fontSize: '1rem' }}>Hindsight Recall</h3>
                      </div>
                      
                      {analysisResult.memoryFound ? (
                        <div className="data-card" style={{ borderColor: 'var(--accent-cyan-dim)', background: 'rgba(0, 210, 255, 0.03)' }}>
                          <div className="data-card-label text-cyan">Historical Context Used</div>
                          
                          <div className="data-card-value" style={{ marginBottom: '1.5rem' }}>
                            {analysisResult.analysis.historicalReference}
                          </div>
                          
                          <div className="data-card-label">Raw Memories Retrieved</div>
                          {analysisResult.recalledMemories.map((mem, i) => (
                            <div key={i} className="hindsight-memory mono">
                              {mem}
                            </div>
                          ))}
                        </div>
                      ) : (
                        <div className="data-card">
                          <div className="data-card-label">Historical Context</div>
                          <div className="data-card-value text-muted">No relevant historical incidents found.</div>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              </>
            )}

            {/* STAGE 3: RESOLUTION & RETENTION */}
            {analysisResult && !resolved && (
              <>
                <div className="flow-connector">
                  <button 
                    className="nav-arrow-button"
                    onClick={() => {
                      setResolveExpanded(true);
                      setTimeout(() => {
                        section3Ref.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
                      }, 50);
                    }}
                    title="Scroll to Resolve & Retain"
                  >
                    <ArrowDown size={24} />
                  </button>
                </div>
                
                <div className="panel" ref={section3Ref}>
                  <div 
                    className="panel-header accordion-header" 
                    onClick={() => setResolveExpanded(!resolveExpanded)}
                  >
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                      <ShieldAlert size={18} style={{ color: 'var(--success-green)' }} />
                      <span className="panel-title">3. Resolve & Retain</span>
                    </div>
                    {resolveExpanded ? <ChevronUp size={18} className="text-muted" /> : <ChevronDown size={18} className="text-muted" />}
                  </div>
                  
                  {resolveExpanded && (
                    <div className="panel-body flex-col gap-4">
                      <div className="data-card">
                        <div className="data-card-label">Confirm Root Cause</div>
                        <input 
                          type="text" 
                          placeholder="What was the actual root cause?"
                          value={resolveForm.rootCause}
                          onChange={e => setResolveForm({...resolveForm, rootCause: e.target.value})}
                        />
                      </div>
                      
                      <div className="data-card">
                        <div className="data-card-label">Resolution Actions Taken</div>
                        <textarea 
                          rows={3}
                          placeholder="Describe the steps taken to resolve the incident..."
                          value={resolveForm.resolution}
                          onChange={e => setResolveForm({...resolveForm, resolution: e.target.value})}
                        />
                      </div>

                      <div className="data-card">
                        <div className="data-card-label">Outcome & Lessons Learned</div>
                        <textarea 
                          rows={2}
                          placeholder="What was the result? How can we prevent this?"
                          value={resolveForm.outcome}
                          onChange={e => setResolveForm({...resolveForm, outcome: e.target.value})}
                        />
                      </div>

                      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem' }}>
                        <button className="btn-outline" onClick={resetFlow}>Cancel</button>
                        <button 
                          className="btn-primary" 
                          onClick={handleResolve}
                          disabled={resolving || !resolveForm.resolution || !resolveForm.outcome}
                        >
                          {resolving ? "Retaining..." : (
                            <>
                              <Database size={18} />
                              Retain Memory
                            </>
                          )}
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              </>
            )}

            {/* STAGE 4: SUCCESS */}
            {resolved && (
              <>
                <div className="flow-connector"><ArrowDown size={24} /></div>
                
                <div className="panel" style={{ borderColor: 'var(--success-green)', background: 'var(--success-green-dim)' }}>
                  <div className="panel-body" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center', padding: '3rem' }}>
                    <CheckCircle2 size={48} style={{ color: 'var(--success-green)', marginBottom: '1rem' }} />
                    <h2 style={{ color: 'var(--success-green)', fontSize: '1.5rem', marginBottom: '0.5rem' }}>Memory Retained</h2>
                    <p style={{ color: 'var(--text-primary)', maxWidth: '400px', margin: '0 auto 2rem auto' }}>
                      The incident resolution has been successfully recorded in Hindsight. OpsMemory will use this knowledge for future incidents.
                    </p>
                    <button className="btn-primary" onClick={resetFlow}>
                      Return to Command Center
                    </button>
                  </div>
                </div>
              </>
            )}

          </div>
        </main>
      </div>
    </div>
  );
}

export default App;
