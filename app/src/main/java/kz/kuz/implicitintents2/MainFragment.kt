package kz.kuz.implicitintents2

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class MainFragment : Fragment() {
    private lateinit var mRecyclerView: RecyclerView

    // цель - вывести список всех приложений (активностей), установленных на телефон
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        activity?.setTitle(R.string.toolbar_title)
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        mRecyclerView = view.findViewById(R.id.recycler_view)
        mRecyclerView.layoutManager = LinearLayoutManager(activity)

        // создаём Intent для запроса активностей, установленных на телефон, у которых в
        // манифесте в intent-filter стоит name="android.intent.action.MAIN"
        val intent = Intent(Intent.ACTION_MAIN)
        // в intent добавляем дополнительный атрибут Category, то есть чтобы в манифесте также
        // стояло name="android.intent.category.LAUNCHER"
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        // создаём эксземпляр класса PackageManager
        // PackageManager управляет приложениями, установленными на телефоне
        val pm = activity!!.packageManager
        // ResolveInfo - это объект, включающий в себя данные, которые PackageManager получает из
        // манифеста активности
        // queryIntentActivities возвращает список объектов ResolveInfo
        // flag 0 обозначает вывести все активности (без фильтров)
        val activities = pm.queryIntentActivities(intent, 0)
        // для запуска активности с помощью неявного интента, он отправляется командой
        // startActivity(intent) либо startActivityForResult(intent)
        // в этом случае выводятся только те активности, у которых в манифесте указано
        // Intent.CATEGORY_DEFAULT, то есть только те активности, которые хотят быть вызваны
        // в ответ на неявный интент
        // queryIntentActivities выводит вообще все приложения, установленные на телефоне
        // если необходим данный фильтр, то его нужно специально включить в дополнение к интенту

        // далее сортируем активности по названию приложения (также указано в манифесте)
        // название приложения вытаскивается командой loadLabel(PackageManager)
        // Collections - фреймворк в языке Java, который содержит методы работы с объектами
        activities.sortWith(Comparator { a, b ->
            val pm = activity?.packageManager
            java.lang.String.CASE_INSENSITIVE_ORDER.compare(
                    a.loadLabel(pm).toString(),
                    b.loadLabel(pm).toString())
        })
        mRecyclerView.adapter = ActivityAdapter(activities)
        return view
    }

    private inner class ActivityHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private var mNameTextView: TextView
        private var mResolveInfo: ResolveInfo? = null
        private val mImageView // добавлено для выполнения задания (добавление иконки)
                : ImageView

        @Throws(PackageManager.NameNotFoundException::class)
        fun bindActivity(resolveInfo: ResolveInfo) {
            mResolveInfo = resolveInfo
            val pm = activity!!.packageManager
            val activityLabel = resolveInfo.loadLabel(pm).toString()

            // если нужно, то ниже способ получить application Label
            val package_name = resolveInfo.activityInfo.packageName
            val applicationLabel = pm.getApplicationLabel(pm.getApplicationInfo(
                    package_name, PackageManager.GET_META_DATA)) as String

            // если нужно узнать данные какого-нибудь приложения:
            val activityInfo = mResolveInfo?.activityInfo
            val packageName = activityInfo?.applicationInfo?.packageName
            val name = activityInfo?.name
            mNameTextView.text = activityLabel
            mImageView.setImageDrawable(resolveInfo.loadIcon(pm)) // добавлено для задания
        }

        override fun onClick(v: View) {
            // ActivityInfo - информация об активности из ResolveInfo
            val activityInfo = mResolveInfo!!.activityInfo
            // ниже другой способ создания ЯВНОГО интента, в нём мы прямо указываем ACTION_MAIN
            // хотя большинство приложений запустятся одинаково, вне зависимости от того,
            // указано это или нет, но некоторые приложения могут повести себя по-другому
            // поэтому лучше явно указать в интенте ACTION_MAIN
//            val i = Intent(Intent.ACTION_MAIN)
//                    .setClassName(activityInfo.applicationInfo.packageName, activityInfo.name)
            // ранее мы использовали другой способ создания интента Intent(Context, Class)
            // в этом случае конструктор определяет класс из конкретной активности, то есть
            // определяет ComponentName, что представляет собой название пакета и класса слитые
            // вместе точно также мы могли бы сконструировать ComponentName и использовать его
            // в следующем конструкторе интента .setComponent(ComponentName)
            val i = Intent(Intent.ACTION_MAIN).setComponent(ComponentName(
                    activityInfo.applicationInfo.packageName, activityInfo.name))
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            // добавляем флаг, указывающий запустить приложение в новой задаче
            startActivity(i)
        }

        // для выполнения задания добавлен макет new_layout
        init {
//            mNameTextView = itemView as TextView // прежний вариант
//            mNameTextView.setOnClickListener(this) // прежний вариант
            val linearLayout = itemView as LinearLayout // добавлено для задания
            mNameTextView = linearLayout.findViewById(R.id.textView) // добавлено для задания
            mImageView = linearLayout.findViewById(R.id.imageView) // добавлено для задания
            linearLayout.setOnClickListener(this)
        }
    }

    private inner class ActivityAdapter(private val mActivities: List<ResolveInfo>) : RecyclerView.Adapter<ActivityHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityHolder {
            val layoutInflater = LayoutInflater.from(activity)
//            val view = layoutInflater.inflate(android.R.layout.simple_list_item_1, parent,
//                    false) // используем встроенный layout андроида (прежний вариант)
            val view = layoutInflater.inflate(R.layout.new_layout, parent,
                    false) // добавлено для задания
            return ActivityHolder(view)
        }

        override fun onBindViewHolder(holder: ActivityHolder, position: Int) {
            val resolveInfo = mActivities[position]
            try {
                holder.bindActivity(resolveInfo)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
        }

        override fun getItemCount(): Int {
            return mActivities.size
        }
    }
}