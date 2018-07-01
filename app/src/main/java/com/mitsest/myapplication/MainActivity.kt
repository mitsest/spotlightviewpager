package com.mitsest.myapplication

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.mitsest.spotlightviewpager.SpotlightViewModel
import com.mitsest.spotlightviewpager.SpotlightViewWrapper

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Handler().postDelayed({


        }, 2000)

        showSpotlight(findViewById(R.id.spotlightView))

    }

    fun showSpotlight(view: SpotlightViewWrapper?) {

        view ?: return
        val targetView: View = findViewById(R.id.target)
        val viewModel = SpotlightViewModel(
                "Lorem Ipsum",
                "Distinctio enim nam fuga. Officiis reprehenderit totam ea vel similique. Deleniti beatae quo aut magnam harum tempore incidunt fuga.",
                targetView)

        val targetView2: View = findViewById(R.id.target2)
        val viewModel2 = SpotlightViewModel(
                "Lorem Ipsum 2",
                "Alias doloribus rerum magnam quam aliquid aliquid. Maxime est mollitia nihil. Ab quia unde facilis adipisci unde aliquam tempora dolores. Eius est dolor qui. Ex voluptatem molestiae sed eveniet beatae. Ipsum esse voluptas est ut sit totam ut sit." +
                        "Delectus consequatur et et reiciendis sed. Voluptatem id omnis est ullam molestias sapiente natus. Voluptas provident laudantium nulla qui." +
                        "Eum aut itaque sed maxime quo voluptatem rerum. Nisi dolore officia molestias. Nihil ut sed optio qui quo vel ea nesciunt. Accusamus nostrum quae iste nam. Cumque iure iure enim. Et aut id occaecati voluptatibus tenetur modi." +
                        "Sit minus repellat voluptatem odit. Quis qui quis ullam ut occaecati possimus corrupti laborum. Saepe quo eligendi excepturi. Aperiam et voluptates quam." +
                        "Distinctio enim nam fuga. Officiis reprehenderit totam ea vel similique. Deleniti beatae quo aut magnam harum tempore incidunt fuga. Enim aut nobis ullam iste eveniet numquam ut ut. Et velit quis odio ipsa enim aut ullam voluptatem. Quae tempore sint eum ratione ratione eligendi excepturi est.",
                targetView2)


        viewModel.next = viewModel2
        view.setFirstTarget(viewModel)

    }
}
