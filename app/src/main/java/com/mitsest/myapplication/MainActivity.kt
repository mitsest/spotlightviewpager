package com.mitsest.myapplication

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.mitsest.spotlightviewpager.view.SpotlightView
import com.mitsest.spotlightviewpager.model.SpotlightViewModel
import com.mitsest.spotlightviewpager.model.SubtitleModel

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Handler().postDelayed({
            showSpotlight()
        }, 1000)

    }

    private fun showSpotlight() {
        val spotlightView = SpotlightView.Builder.getInstance(this)
                .setBackgroundOpacityAnimationDuration(800)
                .setTextOpacityAnimationDuration(600)
                .setSpotlightGrowAnimationDuration(300)
                .setSpotlightPulseAnimationDuration(1200)
                .setMoveAnimationDuration(750)
                .setCloseAnimationDuration(220)
                .build()

        val targetView: View = findViewById(R.id.target)
        val viewModel = SpotlightViewModel(
                "Lorem Ipsum",
                SubtitleModel("Alias doloribus rerum magnam quam aliquid aliquid. Maxime est mollitia nihil. Ab quia unde facilis adipisci unde aliquam tempora dolores. Eius est dolor qui. Ex voluptatem molestiae sed eveniet beatae. Ipsum esse voluptas est ut sit totam ut sit." +
                        "Delectus consequatur et et reiciendis sed. Voluptatem id omnis est ullam molestias sapiente natus. Voluptas provident laudantium nulla qui." +
                        "Eum aut itaque sed maxime quo voluptatem rerum. Nisi dolore officia molestias. Nihil ut sed optio qui quo vel ea nesciunt. Accusamus nostrum quae iste nam. Cumque iure iure enim. Et aut id occaecati voluptatibus tenetur modi." +
                        "Sit minus repellat voluptatem odit. Quis qui quis ullam ut occaecati possimus corrupti laborum. Saepe quo eligendi excepturi. Aperiam et voluptates quam." +
                        "Distinctio enim nam fuga. Officiis reprehenderit totam ea vel similique. Deleniti beatae quo aut magnam harum tempore incidunt fuga. Enim aut nobis ullam iste eveniet numquam ut ut. Et velit quis odio ipsa enim aut ullam voluptatem. Quae tempore sint eum ratione ratione eligendi excepturi est.",
                        15),
                targetView)

        val targetView2: View = findViewById(R.id.target2)
        val viewModel2 = SpotlightViewModel(
                "Lorem Ipsum 2",
                SubtitleModel("Alias doloribus rerum magnam quam aliquid aliquid. Maxime est mollitia nihil. Ab quia unde facilis adipisci unde aliquam tempora dolores. Eius est dolor qui. Ex voluptatem molestiae sed eveniet beatae. Ipsum esse voluptas est ut sit totam ut sit." +
                        "Delectus consequatur et et reiciendis sed. Voluptatem id omnis est ullam molestias sapiente natus. Voluptas provident laudantium nulla qui." +
                        "Eum aut itaque sed maxime quo voluptatem rerum. Nisi dolore officia molestias. Nihil ut sed optio qui quo vel ea nesciunt. Accusamus nostrum quae iste nam. Cumque iure iure enim. Et aut id occaecati voluptatibus tenetur modi." +
                        "Sit minus repellat voluptatem odit. Quis qui quis ullam ut occaecati possimus corrupti laborum. Saepe quo eligendi excepturi. Aperiam et voluptates quam." +
                        "Distinctio enim nam fuga. Officiis reprehenderit totam ea vel similique. Deleniti beatae quo aut magnam harum tempore incidunt fuga. Enim aut nobis ullam iste eveniet numquam ut ut. Et velit quis odio ipsa enim aut ullam voluptatem. Quae tempore sint eum ratione ratione eligendi excepturi est.",
                        9),
                targetView2)

        val targetView3: View = findViewById(R.id.target3)
        val viewModel3 = SpotlightViewModel(
                "Lorem Ipsum 3",
                SubtitleModel("Alias doloribus rerum magnam quam aliquid aliquid. Maxime est mollitia nihil. Ab quia unde facilis adipisci unde aliquam tempora dolores. Eius est dolor qui. Ex voluptatem molestiae sed eveniet beatae. Ipsum esse voluptas est ut sit totam ut sit." +
                        "Delectus consequatur et et reiciendis sed. Voluptatem id omnis est ullam molestias sapiente natus. Voluptas provident laudantium nulla qui." +
                        "Eum aut itaque sed maxime quo voluptatem rerum. Nisi dolore officia molestias. Nihil ut sed optio qui quo vel ea nesciunt. Accusamus nostrum quae iste nam. Cumque iure iure enim. Et aut id occaecati voluptatibus tenetur modi." +
                        "Sit minus repellat voluptatem odit. Quis qui quis ullam ut occaecati possimus corrupti laborum. Saepe quo eligendi excepturi. Aperiam et voluptates quam." +
                        "Distinctio enim nam fuga. Officiis reprehenderit totam ea vel similique. Deleniti beatae quo aut magnam harum tempore incidunt fuga. Enim aut nobis ullam iste eveniet numquam ut ut. Et velit quis odio ipsa enim aut ullam voluptatem. Quae tempore sint eum ratione ratione eligendi excepturi est.",
                        15),
                targetView3)


        SpotlightView.addSpotlightView(this, spotlightView, listOf(viewModel, viewModel2, viewModel3))

    }
}
