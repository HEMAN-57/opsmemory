package com.opsmemory.seeder;

import com.opsmemory.client.HindsightClientService;
import com.opsmemory.client.dto.MemoryItem;
import com.opsmemory.client.dto.RetainResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Seeds Hindsight with historical incident data on application startup.
 *
 * Uses stable document_id values so that re-running the seeder does not
 * create duplicate memories. Before retaining, we check whether the
 * first document already exists in the bank.
 */
@Component
@Order(1)
public class IncidentDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(IncidentDataSeeder.class);
    private static final String SEED_MARKER_DOC_ID = "seed-incident-001";

    private final HindsightClientService hindsight;

    public IncidentDataSeeder(HindsightClientService hindsight) {
        this.hindsight = hindsight;
    }

    @Override
    public void run(String... args) {
        log.info("═══════════════════════════════════════════════");
        log.info("  OpsMemory Incident Data Seeder — Starting");
        log.info("═══════════════════════════════════════════════");

        // 1. Wait for Hindsight to be healthy
        if (!waitForHindsight()) {
            log.error("Hindsight is not reachable. Skipping seed. Start Hindsight and restart the app.");
            return;
        }

        // 2. Check for existing seed data
        if (hindsight.hasDocument(SEED_MARKER_DOC_ID)) {
            log.info("Seed data already exists (document '{}' found). Skipping.", SEED_MARKER_DOC_ID);
            return;
        }

        // 3. Build and retain the seed incidents
        List<MemoryItem> incidents = buildSeedIncidents();
        log.info("Retaining {} seed incidents...", incidents.size());

        try {
            RetainResponse response = hindsight.retain(incidents);
            if (response != null && response.isSuccess()) {
                log.info("✓ Successfully seeded {} incidents into bank '{}'",
                        response.getItemsCount(), response.getBankId());
            } else {
                log.warn("Retain returned unexpected response: {}", response);
            }
        } catch (Exception e) {
            log.error("Failed to seed incidents: {}", e.getMessage(), e);
        }

        log.info("═══════════════════════════════════════════════");
        log.info("  Incident Data Seeder — Complete");
        log.info("═══════════════════════════════════════════════");
    }

    private boolean waitForHindsight() {
        int maxAttempts = 10;
        for (int i = 1; i <= maxAttempts; i++) {
            if (hindsight.isHealthy()) {
                log.info("✓ Hindsight is healthy (attempt {}/{})", i, maxAttempts);
                return true;
            }
            log.info("Waiting for Hindsight... (attempt {}/{})", i, maxAttempts);
            try { Thread.sleep(3000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        return false;
    }

    /**
     * Builds 5 detailed, realistic historical incidents based on the
     * Incident Response Commander specification.
     */
    private List<MemoryItem> buildSeedIncidents() {
        List<MemoryItem> items = new ArrayList<>();

        // ── Incident 1: Payment Service — DB Connection Pool Exhaustion ──
        items.add(new MemoryItem(
                "INCIDENT REPORT — INC-2026-001\n" +
                "Service: payment-service\n" +
                "Severity: SEV2 — Major impact, payments failing for ~40% of users\n" +
                "Date: 2026-08-10T14:23:00Z\n" +
                "Duration: 47 minutes (MTTR)\n" +
                "Detected by: Automated alerting — error rate exceeded 15% threshold\n\n" +
                "SYMPTOMS:\n" +
                "- HTTP 500 errors on /api/payments/process endpoint\n" +
                "- Connection timeout exceptions in logs: 'HikariPool-1 — Connection is not available, request timed out after 30000ms'\n" +
                "- Database connection count at maximum (50/50 active connections)\n" +
                "- Latency p99 spiked from 200ms to 45,000ms\n\n" +
                "ROOT CAUSE:\n" +
                "A deployment at 14:15 introduced a new payment reconciliation query that held database connections for 8-12 seconds instead of the expected 50ms. " +
                "Under normal traffic (200 req/s), connections were acquired faster than they were released, exhausting the HikariCP pool within 8 minutes.\n\n" +
                "RESOLUTION:\n" +
                "1. Rolled back the deployment (14:35) — this stopped new long-running queries\n" +
                "2. Restarted 3 payment-service pods to force-release stuck connections (14:42)\n" +
                "3. Connection pool recovered within 5 minutes after restart\n" +
                "4. All error rates returned to baseline by 15:10\n" +
                "Resolution: SUCCESSFUL\n\n" +
                "LESSONS LEARNED:\n" +
                "- The reconciliation query was not load-tested before deployment\n" +
                "- HikariCP pool size was set to 50 but had no connection timeout configured below 30s\n" +
                "- Action items: (a) Add query execution time limits in the ORM layer, " +
                "(b) Configure HikariCP connectionTimeout to 5000ms, " +
                "(c) Add connection pool utilization to the pre-deployment checklist, " +
                "(d) Implement circuit breaker for database calls exceeding 2s",
                "Post-incident review: payment-service database connection pool exhaustion",
                "seed-incident-001",
                "2026-08-10T15:30:00Z"
        ));

        // ── Incident 2: Auth Service — Expired TLS Certificate ──────────
        items.add(new MemoryItem(
                "INCIDENT REPORT — INC-2026-002\n" +
                "Service: auth-service\n" +
                "Severity: SEV1 — Complete authentication outage, all users affected\n" +
                "Date: 2026-07-22T03:01:00Z\n" +
                "Duration: 23 minutes (MTTR)\n" +
                "Detected by: On-call page — 100% error rate on auth-service\n\n" +
                "SYMPTOMS:\n" +
                "- All login attempts failing with 'SSL handshake failure'\n" +
                "- auth-service unable to connect to the Redis session store\n" +
                "- Error logs: 'javax.net.ssl.SSLHandshakeException: PKIX path validation failed — certificate expired'\n" +
                "- Downstream services returning 401 for all authenticated requests\n\n" +
                "ROOT CAUSE:\n" +
                "The internal TLS certificate used for mTLS between auth-service and the Redis cluster expired at 2026-07-22T03:00:00Z. " +
                "The certificate had a 1-year validity and was manually provisioned. No automated renewal or expiry alerting was configured. " +
                "The certificate was last rotated on 2025-07-22.\n\n" +
                "RESOLUTION:\n" +
                "1. On-call engineer identified the expired cert within 5 minutes via log analysis\n" +
                "2. Generated a new certificate using the internal CA (03:10)\n" +
                "3. Deployed the new certificate to auth-service and Redis via kubectl secret update (03:15)\n" +
                "4. Rolling restart of auth-service pods (03:18)\n" +
                "5. Authentication fully restored by 03:24\n" +
                "Resolution: SUCCESSFUL\n\n" +
                "LESSONS LEARNED:\n" +
                "- Manual certificate management is a ticking time bomb\n" +
                "- No monitoring existed for certificate expiry dates\n" +
                "- Action items: (a) Deploy cert-manager with auto-renewal for all internal certificates, " +
                "(b) Add certificate expiry monitoring with 30-day and 7-day alerts, " +
                "(c) Document the emergency certificate rotation runbook, " +
                "(d) Audit all other internal certificates for upcoming expirations",
                "Post-incident review: auth-service expired TLS certificate causing complete authentication outage",
                "seed-incident-002",
                "2026-07-22T04:00:00Z"
        ));

        // ── Incident 3: Order Service — Deployment Regression ───────────
        items.add(new MemoryItem(
                "INCIDENT REPORT — INC-2026-003\n" +
                "Service: order-service\n" +
                "Severity: SEV2 — Order creation failing, ~60% of new orders rejected\n" +
                "Date: 2026-09-01T10:45:00Z\n" +
                "Duration: 35 minutes (MTTR)\n" +
                "Detected by: Customer support escalation + automated error rate alert\n\n" +
                "SYMPTOMS:\n" +
                "- POST /api/orders returning 400 Bad Request for orders with more than 5 line items\n" +
                "- Error message: 'Validation failed: order items exceed maximum allowed count'\n" +
                "- No errors in staging environment (test data had max 3 items per order)\n" +
                "- Customer complaints began within 10 minutes of deployment\n\n" +
                "ROOT CAUSE:\n" +
                "Version 2.14.0 of order-service introduced a new input validation layer. A developer added a MAX_ITEMS=5 constant " +
                "that was intended as a default for a configurable limit, but it was deployed as a hard-coded constraint. " +
                "Production orders frequently have 10-50 line items (B2B customers). " +
                "The staging environment did not have representative test data, so the regression was not caught.\n\n" +
                "RESOLUTION:\n" +
                "1. Identified the validation regression via git diff between v2.13.2 and v2.14.0 (11:00)\n" +
                "2. Applied a hotfix: changed MAX_ITEMS from 5 to 500 and made it configurable via environment variable (11:08)\n" +
                "3. Fast-tracked deployment of hotfix v2.14.1 to production (11:15)\n" +
                "4. Manually reprocessed 47 failed orders from the dead-letter queue (11:20)\n" +
                "Resolution: SUCCESSFUL\n\n" +
                "LESSONS LEARNED:\n" +
                "- Staging environment test data does not represent production usage patterns\n" +
                "- Validation changes should be flagged for extra review\n" +
                "- Action items: (a) Seed staging with anonymised production data samples, " +
                "(b) Add integration tests covering B2B order sizes (10, 50, 100 items), " +
                "(c) Implement canary deployments for order-service, " +
                "(d) Add dead-letter queue monitoring with automatic alerts",
                "Post-incident review: order-service deployment regression causing order rejection",
                "seed-incident-003",
                "2026-09-01T12:00:00Z"
        ));

        // ── Incident 4: Checkout Service — Database Query Timeout ────────
        items.add(new MemoryItem(
                "INCIDENT REPORT — INC-2026-004\n" +
                "Service: checkout-service\n" +
                "Severity: SEV2 — Checkout flow degraded, 30% of checkouts timing out\n" +
                "Date: 2026-08-28T18:30:00Z\n" +
                "Duration: 52 minutes (MTTR)\n" +
                "Detected by: Automated latency alert — p95 exceeded 10s threshold\n\n" +
                "SYMPTOMS:\n" +
                "- Checkout page loading in 15-30 seconds instead of <2 seconds\n" +
                "- Database CPU at 95% on the primary PostgreSQL instance\n" +
                "- Slow query log showing a cart aggregation query taking 8-12 seconds\n" +
                "- Error logs: 'Statement cancelled due to timeout after 30000 ms'\n\n" +
                "ROOT CAUSE:\n" +
                "A marketing campaign launched at 18:00 drove 3x normal traffic to the checkout flow. " +
                "The cart total aggregation query performed a sequential scan on the order_items table (12M rows) " +
                "because the index on (cart_id, status) had been accidentally dropped during a schema migration 2 weeks earlier (v3.8.0). " +
                "Under normal load, the sequential scan completed in ~500ms, masking the missing index. " +
                "Under 3x load, the scans overwhelmed the database.\n\n" +
                "RESOLUTION:\n" +
                "1. Identified the missing index via EXPLAIN ANALYZE on the slow query (18:50)\n" +
                "2. Created the index with CONCURRENTLY to avoid locking: CREATE INDEX CONCURRENTLY idx_order_items_cart_status ON order_items(cart_id, status) (18:55)\n" +
                "3. Index build completed in ~7 minutes\n" +
                "4. Query time dropped from 8s to 3ms immediately\n" +
                "5. All metrics returned to normal by 19:22\n" +
                "Resolution: SUCCESSFUL\n\n" +
                "LESSONS LEARNED:\n" +
                "- Schema migrations that drop indexes need mandatory review and rollback plans\n" +
                "- Missing indexes can be invisible under normal load but catastrophic under peak traffic\n" +
                "- Action items: (a) Add index existence checks to the CI/CD pipeline, " +
                "(b) Run weekly EXPLAIN ANALYZE on critical query paths, " +
                "(c) Load-test checkout flow at 3x capacity before major marketing campaigns, " +
                "(d) Set up pg_stat_statements monitoring for query plan regressions",
                "Post-incident review: checkout-service database timeout due to missing index under peak traffic",
                "seed-incident-004",
                "2026-08-28T20:00:00Z"
        ));

        // ── Incident 5: Payment Service — Dependency Outage ─────────────
        items.add(new MemoryItem(
                "INCIDENT REPORT — INC-2026-005\n" +
                "Service: payment-service\n" +
                "Severity: SEV1 — Complete payment processing failure\n" +
                "Date: 2026-09-15T09:12:00Z\n" +
                "Duration: 78 minutes (MTTR)\n" +
                "Detected by: Automated alert — payment success rate dropped to 0%\n\n" +
                "SYMPTOMS:\n" +
                "- All payment processing requests failing with 'PaymentGatewayUnavailableException'\n" +
                "- HTTP 503 from the Stripe API gateway\n" +
                "- Circuit breaker tripped on the payment gateway client after 10 consecutive failures\n" +
                "- Revenue impact: estimated $45,000/hour in lost transactions\n\n" +
                "ROOT CAUSE:\n" +
                "Stripe experienced a partial outage affecting their /v1/charges endpoint in the us-east-1 region. " +
                "Our payment-service was configured to use only the primary Stripe endpoint with no fallback. " +
                "The circuit breaker correctly tripped but had no recovery path configured — it required manual reset.\n\n" +
                "RESOLUTION:\n" +
                "1. Confirmed Stripe outage via status.stripe.com (09:20)\n" +
                "2. Attempted to switch to Stripe's eu-west-1 endpoint — discovered our integration did not support multi-region (09:30)\n" +
                "3. Enabled the backup payment processor (PayPal) via feature flag for new transactions (09:45)\n" +
                "4. Stripe service restored at 10:05\n" +
                "5. Reset circuit breaker and re-enabled Stripe as primary processor (10:10)\n" +
                "6. Reprocessed queued payments from the outage window (10:15-10:30)\n" +
                "Resolution: SUCCESSFUL (with workaround)\n\n" +
                "LESSONS LEARNED:\n" +
                "- Single payment processor dependency is a critical business risk\n" +
                "- Circuit breaker needs auto-recovery with exponential backoff, not manual reset\n" +
                "- Action items: (a) Implement automatic failover to backup payment processor, " +
                "(b) Configure circuit breaker with half-open state and exponential backoff recovery, " +
                "(c) Add multi-region support for Stripe integration, " +
                "(d) Create payment queue for automatic retry of failed transactions during outages, " +
                "(e) Conduct quarterly payment failover drills",
                "Post-incident review: payment-service complete failure due to Stripe dependency outage",
                "seed-incident-005",
                "2026-09-15T11:00:00Z"
        ));

        return items;
    }
}
