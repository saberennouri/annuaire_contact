export type Lang = 'fr' | 'ar';

export interface Dict {
  title: string; sub: string; ph: string;
  tabs: { sec: string; pri: string; priv: string; insp: string };
  all: string; fav: string; results: (n: number) => string;
  roles: Record<string, string>;
  mobile: string; fixe: string; tel: string; mail: string; addr: string;
  kinds: Record<string, string>; sub2: Record<string, string>;
  copy: string; copied: string; copyCard: string; more: string;
  none: string; noneSub: string; reset: string; elsewhere: string;
  dark: string; light: string; circ: string; odd: string;
  loading: string; error: string; retry: string;
  src1: string; src2: string; lang: string;
  deleg: Record<string, string>;
}

export const DELEG_AR = ['صفاقس الجنوبية', 'ساقية الداير', 'ساقية الزيت', 'الحنشة', 'منزل شاكر', 'جبنيانة', 'العامرة', 'قرقنة'];

export const DICT: Record<Lang, Dict> = {
  fr: {
    title: 'Annuaire scolaire Sfax 2',
    sub: "Collèges, lycées et écoles primaires de la région : directeurs, cadres, téléphones et e-mails. Année scolaire 2026/2027.",
    ph: 'Établissement, personne, numéro…',
    tabs: { sec: 'Collèges et lycées', pri: 'Écoles primaires', priv: 'Privé primaire et préparatoire', insp: 'Inspections' },
    all: 'Tous', fav: 'Favoris', results: (n) => `${n} ${n > 1 ? 'résultats' : 'résultat'}`,
    roles: { DIRECTOR: 'Directeur(trice)', CENSEUR: 'Censeur', SURVEILLANT_1: 'Surveillant général 1', SURVEILLANT_2: 'Surveillant général 2', ASSISTANT: "Adjoint(e) / chargé(e) de l'administration", CLERK: 'Secrétaire' },
    mobile: 'Mobile', fixe: 'Fixe', tel: 'Téléphone', mail: 'E-mail', addr: 'Adresse',
    kinds: { COLLEGE: 'Collège', LYCEE: 'Lycée', PRIVATE: 'Privé' },
    sub2: { PRIVATE_PRIMARY: 'École primaire privée', PRESCHOOL: 'Classe préparatoire privée' },
    copy: 'Copier', copied: 'Copié', copyCard: 'Copier la fiche', more: 'Afficher la suite',
    none: 'Aucun résultat', noneSub: 'Essayez un autre mot, ou retirez les filtres.', reset: 'Réinitialiser', elsewhere: 'Autres onglets :',
    dark: 'Sombre', light: 'Clair', circ: 'Circonscription', odd: 'Numéro incomplet dans le document source',
    loading: 'Chargement…', error: "Le serveur ne répond pas.", retry: 'Réessayer',
    src1: "Sources : agenda des collèges et lycées de Sfax 2 (version du 23/10/2026) et annuaire téléphonique des écoles primaires (fichier du 10/09/2026).",
    src2: "Application non officielle, élaborée à partir de ces deux documents. Vérifiez auprès de l'administration avant tout usage officiel.",
    lang: 'عربي',
    deleg: { 'صفاقس الجنوبية': 'Sfax Sud', 'ساقية الداير': 'Sakiet Eddaïer', 'ساقية الزيت': 'Sakiet Ezzit', 'الحنشة': 'El Hencha', 'منزل شاكر': 'Menzel Chaker', 'جبنيانة': 'Jebiniana', 'العامرة': 'El Amra', 'قرقنة': 'Kerkennah' },
  },
  ar: {
    title: 'دليل المؤسسات التربوية بصفاقس 2',
    sub: 'الإعداديات والمعاهد والمدارس الابتدائية: المديرون والإطار والهواتف والبريد الإلكتروني. السنة الدراسية 2026/2027.',
    ph: 'مؤسسة، شخص، رقم هاتف…',
    tabs: { sec: 'الإعداديات والمعاهد', pri: 'المدارس الابتدائية', priv: 'الخاص الابتدائي والتحضيري', insp: 'دوائر التفقد' },
    all: 'الكل', fav: 'المفضلة', results: (n) => `${n} نتيجة`,
    roles: { DIRECTOR: 'المدير', CENSEUR: 'الناظر', SURVEILLANT_1: 'القيم العام 1', SURVEILLANT_2: 'القيم العام 2', ASSISTANT: 'مساعد المدير / مكلف بعمل إداري', CLERK: 'الكاتب' },
    mobile: 'الجوال', fixe: 'القار', tel: 'الهاتف', mail: 'البريد', addr: 'العنوان',
    kinds: { COLLEGE: 'إعدادية', LYCEE: 'معهد', PRIVATE: 'خاص' },
    sub2: { PRIVATE_PRIMARY: 'مدرسة ابتدائية خاصة', PRESCHOOL: 'قسم تحضيري خاص' },
    copy: 'نسخ', copied: 'تم النسخ', copyCard: 'نسخ البطاقة', more: 'عرض المزيد',
    none: 'لا توجد نتائج', noneSub: 'جرّب كلمة أخرى أو ألغِ المرشحات.', reset: 'إعادة الضبط', elsewhere: 'في أقسام أخرى:',
    dark: 'داكن', light: 'فاتح', circ: 'دائرة', odd: 'رقم ناقص في الوثيقة الأصلية',
    loading: 'جارٍ التحميل…', error: 'الخادم لا يستجيب.', retry: 'إعادة المحاولة',
    src1: 'المصادر: أجندا المدارس الإعدادية والمعاهد بصفاقس 2 (نسخة 23/10/2026) ودليل هواتف المدارس الابتدائية (ملف 10/09/2026).',
    src2: 'تطبيق غير رسمي أُعدّ انطلاقا من الوثيقتين. يُرجى التثبّت لدى الإدارة قبل أي استعمال رسمي.',
    lang: 'Français',
    deleg: {},
  },
};
