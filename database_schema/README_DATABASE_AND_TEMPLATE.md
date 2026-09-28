# Zadira Live - Architecture & Database Integration Blueprint

This directory contains the external database schemas, stored procedure templates, and dynamic configuration specifications for **Zadira Live**.

## Key Features Controlled via Database (`database_schema/` & `assets/config/server_master_config.json`)
1. **Complete Visual & Style Customization (`app_theme_style_config`)**:
   - Colors, metallic gold gradients, crystal borders, button corner radii, 3D bevel thicknesses, and 7D particle effects.
2. **Administrative Roles & Hierarchy (`role_definitions` & `users`)**:
   - `SUPER_ADMIN` (سوبر أدمن)
   - `ADMIN` (أدمن)
   - `MANAGER` (مدير)
   - `BD` (بدي - تطوير الأعمال وإدارة الوكالات)
   - `CHARGE_AGENT` (وكيل شحن)
   - `HOST_AGENT` (وكيل مضيفين)
   - `HOST` (مضيف)
   - `SUPPORTER` (داعم)
3. **Core Database Operations (`ServerAdminRepository.kt`)**:
   - Special ID assignment (`آي دي مميز`)
   - Entrance Welcome SVGA assignment (`الترحيبيات`)
   - Level & VIP elevation (`رفع المستوى`)
   - Account Ban / Unban (`حظر الحساب`)
   - Agency Recharge & Host Management (`الوكالات والشحن`)
